package nl.infcomtec.personai.minigw;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Server.
 *
 * @author walter
 */
public class Server extends Thread {

    public static final String MARK_START = "$#";
    public static final String MARK_END = "#$";

    private Session session;
    private final boolean clipboardMode = true;
    private final Semaphore step = new Semaphore(1);
    private S state = S.getQuestion;
    private final SystemClipboard cb;
    private final int MAX_OUTPUT = 20000;

    public Server() {
        if (clipboardMode) {
            cb = new SystemClipboard();
        } else {
            cb = null;
        }
        startVagrant();
    }

    /**
     * Keep things controllable. Should show some indication to the user.
     *
     * @param l time we waited in milliseconds.
     */
    private void tellUserWeAreWaiting(long l) {
        // TODO should probably also allow the user to abort.
    }

    /**
     * Parse and execute.
     *
     * @param text
     * @return
     */
    private String exec(String text) {
        StringBuilder sb = new StringBuilder(text);
        StringBuilder output = new StringBuilder();
        int cmdStart = sb.indexOf(MARK_START);
        while (cmdStart >= 0) {
            int cmdEnd = sb.indexOf(MARK_END, cmdStart);
            if (cmdEnd > 2) {
                String cmd = sb.substring(cmdStart + 2, cmdEnd).trim(); // in case the LLM got fancy with whitespace
                execOnBox(cmd, output);
                sb.delete(cmdStart, cmdEnd + 2);
                cmdStart = sb.indexOf(MARK_START);
            }
        }
        // any non-whitespace left plus any output
        if (sb.length() > MAX_OUTPUT) {
            sb.setLength(MAX_OUTPUT / 2);
            sb.append(System.lineSeparator()).append("*** TRUNCATED ***");
        }
        if (output.length() + sb.length() > MAX_OUTPUT) {
            output.setLength(MAX_OUTPUT / 2);
            output.append(System.lineSeparator()).append("*** TRUNCATED ***");
        }
        String disp = sb.toString().trim() + System.lineSeparator() + output.toString();
        if (!disp.isEmpty()) {
            JOptionPane.showMessageDialog(null, disp);
        }
        return disp;
    }

    private void execOnBox(final String cmd, final StringBuilder output) {
        try {
            final Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(cmd);

            final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
            ((ChannelExec) channel).setErrStream(errStream);

            channel.connect();

            final InputStream in = channel.getInputStream();
            final byte[] tmp = new byte[1024];
            while (true) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                output.append(new String(tmp, 0, i));
                if (channel.isClosed()) {
                    break;
                }
            }

            // Append stderr to output
            output.append(errStream.toString());

            channel.disconnect();
        } catch (Exception e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public void run() {
        String text = "";
        while (true) {
            long time = System.currentTimeMillis();
            try {
                while (!step.tryAcquire(1, TimeUnit.SECONDS)) {
                    tellUserWeAreWaiting(System.currentTimeMillis() - time);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            switch (state) {
                case getQuestion: // TODO
                    if (clipboardMode) {
                        text = JOptionPane.showInputDialog("What is your question");
                        state = S.sendQuestion;
                        step.release();
                    } // TODO else
                    break;
                case processAnswer: // TODO
                    if (clipboardMode) {
                        for (; text.isEmpty(); text = cb.getText()) {
                            try {
                                sleep(100);
                            } catch (InterruptedException ex) {
                                System.err.println("Interrupted");
                                System.exit(0);
                            }
                        }
                        text = exec(text);
                        state = S.sendQuestion;
                        step.release();
                    } // TODO else
                    break;
                case sendQuestion:
                    if (clipboardMode) {
                        cb.putText(text);
                        text = "";
                        state = S.getAnswer;
                        step.release();
                    } // TODO else
                    break;
                case getAnswer:
                    if (clipboardMode) {
                        JOptionPane.showMessageDialog(null, "Question is on the clipboard, replace with answer and hit OK");
                        state = S.processAnswer;
                        step.release();
                    }
            }
        }
    }

    private void startVagrant() {
        final JFrame gui = new JFrame("Mini Gateway");
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = gui.getContentPane();
        pane.setLayout(new BorderLayout());
        pane.add(new JLabel("Please wait, starting vagrant box."), BorderLayout.CENTER);
        gui.pack();
        try {
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    gui.setVisible(true);
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        {
            ProcessBuilder pb = new ProcessBuilder("vagrant", "up");
            pb.directory(MiniGW.VAGRANT_DIR);
            pb.inheritIO();
            try {
                Process prc = pb.start();
                int rc = prc.waitFor();
                if (0 != rc) {
                    throw new RuntimeException("Failed to start vagrant, rc=" + rc);
                }
            } catch (Exception ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        }
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(MiniGW.VAGRANT_KEY.getAbsolutePath());

            session = jsch.getSession("vagrant", "localhost", 2222);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();
        } catch (Exception e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            System.exit(1);
        }

        pane.removeAll();
        Box vert = Box.createVerticalBox();
        vert.add(new JLabel("MiniGW and vagrant up and running."));
        vert.add(new JButton(new AbstractAction("Halt and exit") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                {
                    ProcessBuilder pb = new ProcessBuilder("vagrant", "halt");
                    pb.directory(MiniGW.VAGRANT_DIR);
                    pb.inheritIO();
                    try {
                        Process prc = pb.start();
                        int rc = prc.waitFor();
                        if (0 != rc) {
                            throw new RuntimeException("Failed to stop vagrant, rc=" + rc);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        System.exit(1);
                    }
                    System.exit(0);
                }
            }
        }));
        pane.add(vert, BorderLayout.CENTER);
        gui.pack();
        step.release();
    }

    private enum S {
        getQuestion, sendQuestion, processAnswer, getAnswer
    }

}
