package de.tub.group13.ex3;

import de.tub.group13.ex3.model.Message;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import static de.tub.group13.ex3.Helper.*;

public class MainCamel {

    public MainCamel() throws Exception {
        DefaultCamelContext ctxt = new DefaultCamelContext();
        ActiveMQComponent activeMQComponent = ActiveMQComponent.activeMQComponent();
        activeMQComponent.setTrustAllPackages(true);
        ctxt.addComponent("activemq", activeMQComponent);

        RouteBuilder route = new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                // from NEW_ORDER Channels
                from("activemq:queue:NEW_ORDER")
                        // enrich a collerationId for later aggregation
                        .process(new CorrelationIdEnricher())
                        // multicast to to Billing and Inventory channels
                        .multicast()
                        .parallelProcessing()
                        .to("activemq:topic:billingIn", "activemq:topic:inventoryIn")
                        .end();


                // aggregate the validation results
                from("activemq:queue:billInvAggregation")
                        .aggregate(new Expression() {
                            @Override
                            public <T> T evaluate(Exchange exchange, Class<T> type) {
                                return (T) (String.valueOf(inBody(exchange).getOrderID()));
                            }
                        }, new ValidationAggregator()).completionSize(2)
                        // route content-based on different result-queues
                        .choice()
                        .when(header("validationResultHeader").isEqualTo(true)).to("activemq:queue:successfulOrders")
                        .otherwise().to("activemq:queue:failedOrders");

            }
        };

        ctxt.addRoutes(route);
        ctxt.start();
        System.in.read();
        ctxt.stop();
    }

    public static void main(String[] args) throws Exception {
        new MainCamel();
    }

    private class ValidationAggregator implements AggregationStrategy {
        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            if (oldExchange == null) {
                return newExchange;
            } else {
                // set validity according to both results
                Message oldExchangeBody = inBody(oldExchange);
                Message newExchangeBody = inBody(newExchange);
                newExchangeBody.setValid(oldExchangeBody.isValid() && newExchangeBody.isValid());
                String validationResult = Helper.eraseNull(oldExchangeBody.getValidationResult() + " " + newExchangeBody.getValidationResult());
                newExchangeBody.setValidationResult(validationResult);
                newExchange.getOut().setHeader("validationResultHeader",newExchangeBody.isValid());
                return newExchange;
            }
        }
    }

    // set a unique order ID, we just use an incrementing int, since we have just one MainCamel running.
    // Else a UUID or id from a DB would be more appropriate
    private class CorrelationIdEnricher implements org.apache.camel.Processor {
        private AtomicInteger everIncreasingInt = new AtomicInteger(0);

        @Override
        public void process(Exchange exchange) throws Exception {
            exchange.getOut().setBody(exchange.getIn().getBody());
            outBody(exchange).setOrderID(everIncreasingInt.getAndIncrement());
        }
    }
}
