/*
 */
package nl.infcomtec.advswing;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import nl.infcomtec.pubsub.BlobPool;
import nl.infcomtec.pubsub.TextBlob;

/**
 * A sharable JEditorPane.
 *
 * @author Walter Stroebel.
 */
public class AEditorPane extends JEditorPane {

    private final TextBlob blob;
    private final TextBlob.TextType dispType;
    private String topicTx;
    private String topicRx;

    /**
     * Keeps the text in a TextBlob.
     *
     * @param pool for creating the TextBlob.
     * @param topicName Topic in the pool.
     * @param type HTML, MarkDown or Plain.
     */
    public AEditorPane(BlobPool pool, String topicName, TextBlob.TextType type) {
        this.topicTx = topicName + "Tx";
        this.topicRx = topicName + "Rx";
        blob = new TextBlob(pool);
        pool.createTopic(topicTx);
        pool.createTopic(topicRx);
        this.dispType = type;
        switch (dispType) {
            case HTML:
                setContentType("text/html");
                setEditable(false);
                break;
            case MARKDOWN: {
                setContentType("text/plain");
                setEditable(true);
                blob.feedTopic(topicTx, pool.getProducer(topicTx));
                final Document doc = getDocument();
                doc.addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        blob.setMarkDown(getText());
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        blob.setMarkDown(getText());
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        blob.setMarkDown(getText());
                    }
                });
            }
            break;
        }
    }

    public AEditorPane withFont(Font font) {
        super.setFont(font);
        return this;
    }

    /**
     * Get a consumer to receive changes to this field as a message.
     *
     * @return the consumer.
     */
    public BlobPool.Consumer getConsumer() {
        return blob.getConsumer(topicTx);
    }

    /**
     * Keep this pane showing the same text as another pane.
     *
     * @param other Pane to copy.
     * @return For killing.
     */
    public Thread followField(final AEditorPane other) {
        Thread ret = new Thread(new Runnable() {
            @Override
            public void run() {
                BlobPool.Consumer consumer = other.getConsumer();
                while (true) {
                    try {
                        Object tt = consumer.call();
                        if (null != tt) {
                            if (tt instanceof TextBlob.TypedText) {
                                blob.set((TextBlob.TypedText) tt);
                                switch (dispType) {
                                    case HTML:
                                        setText(blob.getHTML());
                                        break;
                                    case MARKDOWN:
                                    case PLAIN:
                                        setText(blob.getPlain());
                                        break;
                                }
                            } else {
                                Logger.getLogger(AEditorPane.class.getName()).log(Level.WARNING,
                                        "Not a TypedText: {0}", tt.getClass().getName());
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(AEditorPane.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        ret.start();
        return ret;
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
        gbc.weightx = 1;
        gbc.weighty = 1;
        Font font = new Font("Arial", Font.PLAIN, 24);
        f.getContentPane().add(new ALabel("HTML").withFont(font), gbc);
        gbc.gridy = 1;
        f.getContentPane().add(new ALabel("MarkDown").withFont(font), gbc);
        gbc.gridy = 2;
        f.getContentPane().add(new ALabel("Plain").withFont(font), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        AEditorPane h, t, c;
        BlobPool pool = new BlobPool();
        f.getContentPane().add(new JScrollPane(h = new AEditorPane(pool, "h", TextBlob.TextType.HTML).withFont(font)), gbc);
        gbc.gridy = 1;
        f.getContentPane().add(new JScrollPane(t = new AEditorPane(pool, "t", TextBlob.TextType.MARKDOWN).withFont(font)), gbc);
        gbc.gridy = 2;
        f.getContentPane().add(new JScrollPane(c = new AEditorPane(pool, "c", TextBlob.TextType.MARKDOWN).withFont(font)), gbc);
        h.followField(t);
        c.followField(t);
        f.setVisible(true);
    }
}
