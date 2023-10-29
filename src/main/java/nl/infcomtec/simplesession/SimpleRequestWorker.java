package nl.infcomtec.simplesession;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import nl.infcomtec.personai.Message;
import nl.infcomtec.personai.OpenAIAPI;
import nl.infcomtec.personai.PersonAI;
import nl.infcomtec.personai.Usage;

/**
 *
 * @author Walter Stroebel
 */
public class SimpleRequestWorker extends SwingWorker<String, String> {

    private final String question;
    private Exception failure;
    private final StringBuilder lastInteraction = new StringBuilder();
    private final String system;
    private final String text;
    private final JTextArea progressMsg;
    public String answer;
    public double cost;
    public final AtomicInteger outputTokens = new AtomicInteger(0);
    public final AtomicInteger promptTokens = new AtomicInteger(0);
    public Semaphore completed = new Semaphore(0);

    public SimpleRequestWorker(
            JTextArea progressMsg,
            String system,
            String question,
            String text) {
        this.question = question;
        this.system = system;
        this.text = text;
        this.progressMsg = progressMsg;
    }

    @Override
    protected String doInBackground() {
        long stCall = System.currentTimeMillis();
        try {
            if (!text.isEmpty()) {
                answer = OpenAIAPI.makeRequest(new Message[]{
                    new Message(Message.ROLES.system, system),
                    new Message(Message.ROLES.user, question),
                    new Message(Message.ROLES.assistant, text)
                });
            } else {
                answer = OpenAIAPI.makeRequest(new Message[]{
                    new Message(Message.ROLES.system, system),
                    new Message(Message.ROLES.user, question)
                });
            }
            // XXX cost calculation
            for (Iterator<Usage> it = OpenAIAPI.usages.iterator(); it.hasNext();) {
                Usage us = it.next();
                it.remove();
                promptTokens.addAndGet(us.promptTokens);
                outputTokens.addAndGet(us.completionTokens);
            }
            publish(String.format("Call tokens:\n  %5d in\n  %5d response",
                    promptTokens.get(), outputTokens.get()));
            cost = promptTokens.get() * PersonAI.ITC + outputTokens.get() * PersonAI.OTC;
            publish(String.format("Call time:\n  %5.3f seconds\nCall cost:\n  %5.3f cents",
                    (System.currentTimeMillis() - stCall) * 1E-3, cost * 100));
        } catch (Exception e) {
            this.failure = e;
        }

        return "";
    }

    @Override
    protected void process(List<String> chunks) {
        for (String msg : chunks) {
            progressMsg.append(System.lineSeparator());
            progressMsg.append(msg);
        }
    }

    @Override
    protected void done() {
        if (failure != null) {
            // If an exception occurred, update the status message
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressMsg.setForeground(Color.red.brighter());
                    progressMsg.append(failure.getMessage());
                }
            });
        } else {
            progressMsg.setForeground(Color.yellow);
        }
        completed.release();
    }

}
