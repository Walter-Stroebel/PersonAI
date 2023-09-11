package nl.infcomtec.llmtools.minigw;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Server.
 *
 * @author walter
 */
public class Server extends Thread {
    
    private enum S {
        init,
    };
    
    public Server() {
        startVagrant();
    }
    
    @Override
    public void run() {
        
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
        vert.add(new JButton(new AbstractAction("(Re)start MiniGW client") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                MiniGW.startClient();
            }
        }));
        pane.add(vert, BorderLayout.CENTER);
        gui.pack();
    }
    
}
