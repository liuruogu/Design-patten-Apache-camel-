package de.tub.group13.ex3;

import de.tub.group13.ex3.model.Message;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class Helper implements Processor {
    public static Helper DEFAULT = new Helper();

    @Override
    public void process(Exchange exchange) throws Exception {
        System.out.println(exchange.getIn().toString());
        exchange.setOut(exchange.getIn());
    }

    public static Message inBody(Exchange exchange) {
        return ((Message) exchange.getIn().getBody());
    }

    public static Message outBody(Exchange exchange) {
        return ((Message) exchange.getOut().getBody());
    }

    public static String eraseNull(String input){
        return input.replaceAll("null","").replaceAll("  "," ");
    }
}
