/*
 */
package nl.infcomtec.advswing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import nl.infcomtec.pubsub.BlobPool;

/**
 *
 * @author walter
 */
public class AEditorPane extends JEditorPane {

    private final TextBlob blob = new TextBlob();
    private final TextBlob.TextType dispType;
    private String topicTx;
    private String topicRx;
    private final BlobPool pool;

    public AEditorPane(String topicName, TextBlob.TextType type) {
        this.topicTx = topicName + "Tx";
        this.topicRx = topicName + "Rx";
        this.pool = new BlobPool().withTopics(this.topicRx,this.topicTx);
        BlobPool.expireSeconds.set(1);
        this.pool.hireCleaner();
        this.dispType = type;
        switch (dispType) {
            case HTML:
                setContentType("text/html");
                setEditable(false);
                break;
            case PLAIN: {
                setContentType("text/plain");
                setEditable(true);
                blob.feedTopic(topicTx, pool.getProducer(topicTx));
                final Document doc = getDocument();
                doc.addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        blob.setPlain(getText());
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        blob.setPlain(getText());
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        blob.setPlain(getText());
                    }
                });
            }
            break;
        }
        // TODO need to update text on changes.
    }

    /**
     * Get a consumer to receive changes to this field as a message.
     *
     * @return the consumer.
     */
    public BlobPool.Consumer<TextBlob.TypedText> getConsumer() {
        return pool.getConsumer(topicRx);
    }

    /**
     * Demo.
     *
     * @param args
     */
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1200, 600);
        f.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        f.getContentPane().add(new JLabel("HTML"), gbc);
        gbc.gridx = 1;
        f.getContentPane().add(new JLabel("MarkDown"), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.2;
        gbc.weighty = 0.9;
        AEditorPane h, t;
        f.getContentPane().add(new JScrollPane(h = new AEditorPane("h", TextBlob.TextType.HTML)), gbc);
        gbc.gridx = 1;
        f.getContentPane().add(new JScrollPane(t = new AEditorPane("t", TextBlob.TextType.PLAIN)), gbc);
        f.setVisible(true);
        System.out.println(t.pool.listTopics());
        BlobPool.Consumer<TextBlob.TypedText> consumer = t.pool.getConsumer("ttx");
        while (true) {
            try {
                TextBlob.TypedText tt = consumer.call();
                if (null != tt) {
                    System.out.println(tt);
                    h.setText(t.blob.getHTML());
                }
            } catch (Exception ex) {
                Logger.getLogger(AEditorPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
