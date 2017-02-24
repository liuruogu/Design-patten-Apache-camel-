package de.tub.group13.ex3.billingsystem;

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

public class BillingSystemApplication {

    private final Session session;
    private final Queue outQueue;

    public BillingSystemApplication() throws Exception {
        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory();
        conFactory.setTrustAllPackages(true);
        Connection con = conFactory.createConnection();

        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic inTopic = session.createTopic("billingIn");
        outQueue = session.createQueue("billInvAggregation");
        MessageConsumer consumer = session.createConsumer(inTopic);

        consumer.setMessageListener(this::checkCustomerCredibility);

        con.start();
        System.in.read();
        con.close();
    }

    public static void main(String[] args) throws Exception {
        new BillingSystemApplication();
    }

    // check customer credibility, we use Math.random to randomly sort out some customers.
    public void checkCustomerCredibility(javax.jms.Message message) {
        try {
            Message msg = (Message) ((ObjectMessage) message).getObject();
            String result = "";
            if (Math.random() < 0.1) {
                result = "Customer not credible.";
                msg.setValid(false);
            } else {
                msg.setValid(true);
            }
            msg.setValidationResult(result);
            System.out.println("Processed Order with ID " + msg.getOrderID() + " result: " + result);

            ObjectMessage answer = session.createObjectMessage(msg);
            MessageProducer producer = session.createProducer(outQueue);
            producer.send(answer);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
