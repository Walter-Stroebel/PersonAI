package nl.infcomtec.advswing;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;
import nl.infcomtec.pubsub.Consumer;

/**
 *
 * @author walter
 */
public class ATextField extends JTextField {

    public ATextField(final String topicName, String text) {
        super(text);
        final Consumer<String> stringUTF = Consumer.stringUTF(topicName);
        stringUTF.setAutoTask(new AutoUpdate(stringUTF));
    }

    public ATextField(String topicName, int columns) {
        super(columns);
        final Consumer<String> stringUTF = Consumer.stringUTF(topicName);
        stringUTF.setAutoTask(new AutoUpdate(stringUTF));
    }

    public ATextField(String topicName, String text, int columns) {
        super(text, columns);
        final Consumer<String> stringUTF = Consumer.stringUTF(topicName);
        stringUTF.setAutoTask(new AutoUpdate(stringUTF));
    }

    public ATextField(String topicName) {
        final Consumer<String> stringUTF = Consumer.stringUTF(topicName);
        stringUTF.setAutoTask(new AutoUpdate(stringUTF));
    }

    private class AutoUpdate implements Runnable {

        private final Consumer<String> stringUTF;

        public AutoUpdate(Consumer<String> stringUTF) {
            this.stringUTF = stringUTF;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String msg = stringUTF.getMessageFromTopic();
                    if (null == msg) {
                        break;
                    }
                    setText(msg);
                }
            } catch (Exception ex) {
                Logger.getLogger(ATextField.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
