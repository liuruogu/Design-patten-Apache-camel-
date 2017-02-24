package de.tub.group13.ex3.inventorysystem;

import de.tub.group13.ex3.model.Message;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;

public class InventorySystemApplication {


    private final Object inventoryLock = new Object();
    private final Session session;
    private final Queue outQueue;
    private int divingSuits = 125;
    private int surfboards = 95;

    public InventorySystemApplication() throws Exception {
        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory();
        conFactory.setTrustAllPackages(true);
        Connection con = conFactory.createConnection();

//      buckysession = con.notifyAll();
        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic inTopic = session.createTopic("inventoryIn");
        outQueue = session.createQueue("billInvAggregation");
        MessageConsumer consumer = session.createConsumer(inTopic);

        consumer.setMessageListener(this::checkInventory);

        con.start();
        System.in.read();
        con.close();
    }

    public static void main(String[] args) throws Exception {
        new InventorySystemApplication();
    }

    // check inventory and set valid to false in case there is not enough on stock of the products.
    public void checkInventory(javax.jms.Message message) {
        try {
            Message msg = (Message) ((ObjectMessage) message).getObject();
            String result = "";
            synchronized (inventoryLock) {

                inventoryLock.notifyAll();

                if (msg.getNumberOfSurfboards() > surfboards) {
                    result += "Not enough surfboards. ";
                }
                if (msg.getNumberOfDivingSuits() > divingSuits) {
                    result += "Not enough divingSuits.";
                }
                if (!result.isEmpty()) {
                    msg.setValidationResult(result);
                    msg.setValid(false);
                } else {
                    msg.setValid(true);
                }

            }

            System.out.println("Processed Order with ID " + msg.getOrderID() + " result: " + result);

            ObjectMessage answer = session.createObjectMessage(msg);
            MessageProducer producer = session.createProducer(outQueue);
            producer.send(answer);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}