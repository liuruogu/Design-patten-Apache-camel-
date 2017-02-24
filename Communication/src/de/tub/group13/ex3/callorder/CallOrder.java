package de.tub.group13.ex3.callorder;

import de.tub.group13.ex3.model.Message;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class CallOrder {

    public static void main(String[] args) throws Exception {
        Thread orderGenerator = new CallOrderGenerator();
        orderGenerator.start();
        new CallOrder();
    }

    public CallOrder() throws Exception {
        DefaultCamelContext ctxt = new DefaultCamelContext();
        ActiveMQComponent activeMQComponent = ActiveMQComponent.activeMQComponent();
        activeMQComponent.setTrustAllPackages(true);
        ctxt.addComponent("activemq", activeMQComponent);


        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
            // callOrder Message endpoint
            from("file:genOrders?noop=true")
            // translate message
            .split(body().tokenize("\n"))
            .process(CallOrderMessageTranslator)
            .setExchangePattern(ExchangePattern.InOnly)
            // to NEW_ORDER channel
            .to("activemq:queue:NEW_ORDER");
            //.to("stream:out");
            }
        };

        ctxt.addRoutes(route);
        ctxt.start();
        //runSampleOrders(callOrderGateway);
        System.in.read();
        ctxt.stop();
    }

    private static Processor CallOrderMessageTranslator = new Processor() {


        @Override
        public void process(Exchange exchange) throws Exception {
            String inMsg = exchange.getIn().getBody(String.class).replace("\n", "");
            String[] splittedMsg = inMsg.split(", ");

            Message outMsg = new Message();
            outMsg.setCustomerID(splittedMsg[0]);
            String[] splitNames = splittedMsg[1].split(" ");
            outMsg.setFirstName(splitNames[0]);
            outMsg.setLastName(splitNames[1]);
            outMsg.setNumberOfSurfboards(Integer.parseInt(splittedMsg[2]));
            outMsg.setNumberOfDivingSuits(Integer.parseInt(splittedMsg[3].replace(System.getProperty("line.separator"), "")));

            outMsg.setOverallItems(outMsg.getNumberOfDivingSuits() + outMsg.getNumberOfSurfboards());

            exchange.getOut().setBody(outMsg);
        }

    };
    //Generate the orders and restore into the local files.
    private static class CallOrderGenerator extends Thread {

        public String[] listUsers = {"Tom A", "Josh B", "Chad C", "Tommy D", "Sarah E", "Cary F", "Johnny G", "Heather H"};

        public void run() {
            while(true) {
                try(FileWriter fw = new FileWriter(new File("genOrders","genOrders"), true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw))
                {
                    //Create the order using threadlocalrandom with a range of(0,usersnum)
                    int choosenUser = ThreadLocalRandom.current().nextInt(0, listUsers.length);
                    int numberSuitOrders = ThreadLocalRandom.current().nextInt(0, 100 + 1);
                    int numberBoardOrders = ThreadLocalRandom.current().nextInt(0, 100 + 1);
                    String genOrder = choosenUser + ", " + listUsers[choosenUser] + ", " + numberSuitOrders + ", " + numberBoardOrders + "\n";
                    out.print(genOrder);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    //exception handling left as an exercise for the reader
                }
                try {
                    Thread.sleep(12000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

    }

}
