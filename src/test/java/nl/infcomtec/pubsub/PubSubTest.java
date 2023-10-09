package nl.infcomtec.pubsub;

import java.time.Duration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class PubSubTest {

    Topic topic;
    Producer<String> producer;
    Consumer<String> consumer;

    @Before
    public void setUp() {
        topic = Topic.openOrCreate("testTopic");
        producer = Producer.stringUTF("testTopic");
        consumer = Consumer.stringUTF("testTopic");
    }

    @Test
    public void testProduceAndConsume() throws Exception {
        // Test if a message can be produced and then consumed
        String message = "Hello, world!";
        producer.sendMessage(message);

        String receivedMessage = consumer.getMessageFromTopic();
        assertEquals(message, receivedMessage);
    }

    @Test
    public void testConsumeWithTimeout() throws Exception {
        // Test consumer timeout
        String receivedMessage = consumer.getMessageFromTopic(Duration.ofMillis(500));
        assertNull(receivedMessage);
    }

    @Test
    public void testTopicClose() throws Exception {
        // Test if closing a topic works as expected
        topic.close();
    }
}
