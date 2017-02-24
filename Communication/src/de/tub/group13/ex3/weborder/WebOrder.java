package de.tub.group13.ex3.weborder;

import de.tub.group13.ex3.model.Message;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelException;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.ProxyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class WebOrder {

    public WebOrder() throws Exception {
        DefaultCamelContext ctxt = new DefaultCamelContext();
        ActiveMQComponent activeMQComponent = ActiveMQComponent.activeMQComponent();
        activeMQComponent.setTrustAllPackages(true);
        ctxt.addComponent("activemq", activeMQComponent);

        // WebOrder gateway
        final WebOrderGateway webOrderGateway = new ProxyBuilder(ctxt)
                .endpoint("direct:webOrderConsumer").build(WebOrderGateway.class);

        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // WebOrder Message endpoint
                from("direct:webOrderConsumer")
                        .setExchangePattern(ExchangePattern.InOnly)
                        //to WEB_NEW_ORDER Channels
                        .to("activemq:queue:WEB_NEW_ORDER");

                // from WEB_NEW_ORDER Channels
                from("activemq:queue:WEB_NEW_ORDER")
                        // translate message
                        .process(new WebOrderMessageTranslator())
                        // to NEW_ORDER channel
                        .to("activemq:queue:NEW_ORDER");
                ;
            }
        };

        ctxt.addRoutes(route);
        ctxt.start();
        runSampleOrders(webOrderGateway);
        System.in.read();
        ctxt.stop();
    }

    public static void main(String[] args) throws Exception {
        new WebOrder();
    }


    // translates from comma-seperated input orders to or Message object
    public class WebOrderMessageTranslator implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            String inMsg = (String) exchange.getIn().getBody();

            String[] splittedMsg = inMsg.split(",");
            if (splittedMsg.length != 5) {
                throw new CamelException("Invalid input message from WebOrderGateway");
            }

            Message outMsg = new Message();
            outMsg.setFirstName(splittedMsg[0]);
            outMsg.setLastName(splittedMsg[1]);
            outMsg.setNumberOfSurfboards(Integer.parseInt(splittedMsg[2]));
            outMsg.setNumberOfDivingSuits(Integer.parseInt(splittedMsg[3]));
            outMsg.setCustomerID(splittedMsg[4]);

            outMsg.setOverallItems(outMsg.getNumberOfDivingSuits() + outMsg.getNumberOfSurfboards());

            exchange.getOut().setBody(outMsg);
        }

    }

    // produce some random sample orders
    private void runSampleOrders(final WebOrderGateway webOrderGateway) {
        new Thread(() -> {
            while (true) {
                // forever and ever
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int nrOfSurfboards = (int) (Math.random() * 1000) % 5;
                int nrOfDivingSuits = (int) (Math.random() * 1000) % 5;
                int customerId = (int) (Math.random() * 1000) % 10;

                String sampleOrder = "Alice,ImWunderland," + nrOfSurfboards + "," + nrOfDivingSuits + "," + customerId;
                System.out.println("Sending sample order" + sampleOrder);
                webOrderGateway.submitOrderString(sampleOrder);
            }
        }).start();

    }

}
