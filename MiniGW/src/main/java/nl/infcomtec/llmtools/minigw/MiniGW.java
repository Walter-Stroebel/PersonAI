package nl.infcomtec.llmtools.minigw;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class and global members.
 *
 * @author Walter Stroebel
 */
public class MiniGW {

    public static final File HOME_DIR = new File(System.getProperty("user.home"));
    public static final File VAGRANT_DIR = new File(HOME_DIR, "vagrant/MiniGW");
    public static final String CLIENT_START_SH = "clientStart.sh";
    public static final int SERVER_PORT = 9999;
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final LinkedList<VgComm> inbox = new LinkedList<>();
    private static final LinkedList<VgComm> outbox = new LinkedList<>();

    public static void main(String[] args) {
        if (args.length > 0) { // any argument makes us client
            TX(args[0]);
            new Client().start();
        } else {
            new Server().start();
            RX();
        }
    }

    public static void startClient() {
        File haveScript = new File(VAGRANT_DIR, CLIENT_START_SH);
        if (haveScript.exists()) {
            { // cludge, the file might not be executable, insure it is
                ProcessBuilder pb = new ProcessBuilder("vagrant", "ssh", "-c",
                        "chmod 750 /vagrant/" + MiniGW.CLIENT_START_SH);
                pb.directory(MiniGW.VAGRANT_DIR);
                pb.inheritIO();
                try {
                    Process prc = pb.start();
                    int rc = prc.waitFor();
                    if (0 != rc) {
                        throw new RuntimeException("Failed to make start script executable, rc=" + rc);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
            }
            { // cludge, use a script to start client in the background
                ProcessBuilder pb = new ProcessBuilder("vagrant", "ssh", "-c",
                        "/vagrant/" + MiniGW.CLIENT_START_SH);
                pb.directory(MiniGW.VAGRANT_DIR);
                pb.inheritIO();
                try {
                    Process prc = pb.start();
                    int rc = prc.waitFor();
                    if (0 != rc) {
                        throw new RuntimeException("Failed to start vagrant client, rc=" + rc);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
            }
        }
    }

    public static void RX() {
        try {
            final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try (final Socket accept = serverSocket.accept()) {
                            System.out.println("Got connection from " + accept.getRemoteSocketAddress());
                            xmit(accept);
                        } catch (Exception any) {
                            System.err.println("Tool (re)connecting? " + any.getMessage());
                        }
                    }
                }
            }.start();
        } catch (IOException ex) {
            Logger.getLogger(MiniGW.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void TX(String arg) {
        try (final Socket client = new Socket("10.0.2.2", SERVER_PORT)) {
            xmit(client);
            System.exit(0);
        } catch (Exception ex) {
            Logger.getLogger(MiniGW.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
    }

    public static VgComm rxMessage() {
        synchronized (inbox) {
            while (inbox.isEmpty()) {
                try {
                    inbox.wait();
                } catch (InterruptedException ex) {
                    System.exit(0);
                }
            }
            return inbox.removeFirst();
        }
    }

    public static void txMessage(VgComm msg) {
        synchronized (outbox) {
            outbox.add(msg);
            outbox.notifyAll();
        }
    }

    public static void xmit(final Socket sock) {
        Thread vin = new Thread(new Runnable() {
            @Override
            public void run() {
                try ( DataInputStream dis = new DataInputStream(sock.getInputStream())) {
                    while (true) {
                        synchronized (inbox) {
                            inbox.add(gson.fromJson(dis.readUTF(), VgComm.class));
                            inbox.notifyAll();
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MiniGW.class.getName()).log(Level.SEVERE, null, ex);
                    try {
                        sock.close();
                        // make sure tx is not stuck in wait.
                        txMessage(new VgComm());
                    } catch (IOException ex1) {
                        // ignore
                    }
                }
            }
        }, "VagrantIn");
        vin.start();
        Thread vout = new Thread(new Runnable() {
            @Override
            public void run() {
                try ( DataOutputStream dos = new DataOutputStream(sock.getOutputStream())) {
                    while (true) {
                        synchronized (outbox) {
                            while (outbox.isEmpty()) {
                                outbox.wait();
                            }
                            VgComm msg = outbox.removeFirst();
                            dos.writeUTF(gson.toJson(msg));
                            dos.flush();
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(MiniGW.class.getName()).log(Level.SEVERE, null, ex);
                    try {
                        sock.close();
                    } catch (IOException ex1) {
                        // ignore
                    }
                }
            }
        }, "VagrantOut");
        vout.start();
        try {
            vin.join();
            vout.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(MiniGW.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
