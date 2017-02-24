package de.tub.group13.ex3.resultsystem;

import de.tub.group13.ex3.model.Message;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

public class ResultSystemApplication {

    private final Session session;

    public ResultSystemApplication() throws Exception {
        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory();
        conFactory.setTrustAllPackages(true);
        Connection con = conFactory.createConnection();

        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue successfulOrdersQueue = session.createQueue("successfulOrders");
        Queue failedOrdersQueue = session.createQueue("failedOrders");
        MessageConsumer successfulOrderConsumer = session.createConsumer(successfulOrdersQueue);
        MessageConsumer failedOrderConsumer = session.createConsumer(failedOrdersQueue);

        successfulOrderConsumer.setMessageListener(this::printSucessfulOrders);
        failedOrderConsumer.setMessageListener(this::printFailedOrders);

        con.start();
        System.in.read();
        con.close();
    }

    // prints successful orders (further processing would come here instead)
    public void printSucessfulOrders(javax.jms.Message message) {
        printFailedOrders(message);
    }

    // prints failed orders (further processing would come here instead)
    private void printFailedOrders(javax.jms.Message message) {
        try {
            Message msg = (Message) ((ObjectMessage) message).getObject();
            System.out.println("Received Order: " + msg.toString());
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        new ResultSystemApplication();
    }



}
