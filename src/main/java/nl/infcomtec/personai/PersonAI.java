package nl.infcomtec.personai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import nl.infcomtec.graphs.ClEdge;
import nl.infcomtec.graphs.ClGraph;
import nl.infcomtec.graphs.ClNode;
import nl.infcomtec.simpleimage.ImageObject;
import nl.infcomtec.simpleimage.ImageViewer;
import nl.infcomtec.simpleimage.Marker;

public class PersonAI {

    public static Font font = new Font(Font.SERIF, Font.PLAIN, 24);
    public static final String osName = System.getProperty("os.name").toLowerCase();
    public static final File HOME_DIR = new File(System.getProperty("user.home"));
    public static final File WORK_DIR = new File(PersonAI.HOME_DIR, ".personAI");
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final File VAGRANT_DIR = new File(PersonAI.HOME_DIR, "vagrant/MiniGW");
    public static final File VAGRANT_KEY = new File(VAGRANT_DIR, ".vagrant/machines/default/virtualbox/private_key");

    public static void main(String[] args) throws Exception {
        PersonAI.WORK_DIR.mkdirs(); // in case it doesn't
        if (!PersonAI.WORK_DIR.exists()) {
            System.err.println("Cannot access nor create work directory?");
            System.exit(1);
        }
        new PersonAI();
    }
    public JFrame frame;
    public JToolBar toolBar;
    public JTabbedPane tabbedPane;
    public ImageObject dot;
    private JTextArea topic;
    private Instructions ins;
    private Instruction curIns;
    public ClGraph graph = new ClGraph();
    private ImageViewer dotViewer;
    private Vagrant vagrant;

    public PersonAI() throws IOException {
        initGUI();
        setVisible();
    }

    private void setFont() {
        try ( BufferedReader bfr = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/uimanager.fontKeys")))) {
            if (null != frame) {
                frame.dispose();
            }
            for (String key = bfr.readLine(); null != key; key = bfr.readLine()) {
                UIManager.put(key, font);
            }
        } catch (IOException ex) {
            // we tried ... might not be fatal
            System.err.println(ex.getMessage());
        }
    }

    private void initGUI() {
        try {
            dot = new ImageObject(ImageIO.read(getClass().getResourceAsStream("/robotHelper.png")));
        } catch (IOException ex) {
            Logger.getLogger(PersonAI.class.getName()).log(Level.SEVERE, null, ex);
        }
        setFont();
        frame = new JFrame("PersonAI");
        toolBar = new JToolBar();
        tabbedPane = new JTabbedPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Maximize and set always on top
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setAlwaysOnTop(true);

        // Add JToolBar to the north
        frame.getContentPane().add(toolBar, BorderLayout.NORTH);

        // Add JTabbedPane to the center
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        JPanel main = new JPanel(new BorderLayout());
        dotViewer = new ImageViewer(dot);
        JPanel viewPanel = dotViewer.getScalePanPanel();
        dot.addListener(new ImageObject.ImageObjectListener("Mouse") {
            @Override
            public void mouseEvent(ImageObject imgObj, ImageObject.MouseEvents ev, MouseEvent e) {
                ClNode node = graph.getNode(e);
                System.out.println(node.getName() + ": " + node.label);
                System.out.println(graph.nodeCenters);
                System.out.println(graph.segments.keySet());
                Marker m = new Marker(graph.segments.get(node.getName()), 0xFFE0E0E0, 0x7F0000);
                dotViewer.clearMarkers();
                dotViewer.addMarker(m);
                frame.repaint();
            }
        });
        main.add(viewPanel, BorderLayout.CENTER);
        JPanel box = new JPanel(new BorderLayout());
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        DisplayMode dm = defaultScreen.getDisplayMode();
        box.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        box.setPreferredSize(new Dimension(dm.getWidth() / 4, dm.getHeight() * 70 / 100));
        JPanel pan = new JPanel(new FlowLayout());
        JScrollPane inpPan = new JScrollPane(new JTextArea(20, 64));
        inpPan.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE, 5), "Type a message here:"));
        topic = new JTextArea();
        JScrollPane topPan = new JScrollPane(topic);
        topPan.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE, 5), "Selected text:"));
        topPan.setPreferredSize(new Dimension(dm.getWidth(), dm.getHeight() * 30 / 100));
        ins = Instructions.load(new File(WORK_DIR, "instructions.json"), gson);
        pan.add(new JLabel("Pick an operation: "));
        for (final Instruction i : ins.insList) {
            JButton jb = new JButton(new AbstractAction(i.description) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    curIns = i;
                }
            });
            jb.setToolTipText(i.prompt);
            pan.add(jb);
        }
        box.add(pan, BorderLayout.CENTER);
        inpPan.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE, 5), "Type a message here (optional):"));
        box.add(inpPan, BorderLayout.NORTH);
        box.add(new JButton(new AbstractAction("Send to AI/LLM") {
            @Override
            public void actionPerformed(ActionEvent ae) {

            }
        }), BorderLayout.SOUTH);
        main.add(box, BorderLayout.EAST);
        main.add(topPan, BorderLayout.SOUTH);
        tabbedPane.addTab("Main", main);
        tabbedPane.addTab("Tab 2", new JPanel());
        putOnBar(new JButton(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        }));
        putOnBar(new JButton(new AbstractAction("Bigger text") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                font = new Font(font.getName(), font.getStyle(), font.getSize() + 1);
                initGUI();
                setVisible();
            }
        }));
        putOnBar(new JButton(new AbstractAction("Smaller text") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (font.getSize() > 6) {
                    font = new Font(font.getName(), font.getStyle(), font.getSize() - 1);
                    initGUI();
                    setVisible();
                }
            }
        }));
        putOnBar(new JButton(new AbstractAction("Load") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    ins = Instructions.load(new File(PersonAI.WORK_DIR, "instructions.json"), gson);
                    graph.clear();
                    ClNode n1 = graph.addNode(new ClNode(graph, UUID.randomUUID().toString(), "shape=box"));
                    ClNode n2 = graph.addNode(new ClNode(graph, UUID.randomUUID().toString(), "shape=box"));
                    ClNode n3 = graph.addNode(new ClNode(graph, UUID.randomUUID().toString(), "shape=box"));
                    ClNode n4 = graph.addNode(new ClNode(graph, UUID.randomUUID().toString(), "shape=box"));
                    ClNode n5 = graph.addNode(new ClNode(graph, UUID.randomUUID().toString(), "shape=box"));
                    ClNode n6 = graph.addNode(new ClNode(graph, UUID.randomUUID().toString(), "shape=box"));
                    graph.addNode(new ClEdge(n1, n2, "", null));
                    graph.addNode(new ClEdge(n2, n3, "", null));
                    graph.addNode(new ClEdge(n2, n4, "", null));
                    graph.addNode(new ClEdge(n1, n5, "", null));
                    graph.addNode(new ClEdge(n5, n6, "", null));
                    BufferedImage render = graph.render();
                    dot.putImage(render);
                    graph.segments = dot.calculateClosestAreas(graph.nodeCenters);
                    frame.repaint();
                } catch (Exception ex) {
                    Logger.getLogger(PersonAI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
        putOnBar(new JButton(new AbstractAction("Clear") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                frame.dispose();
                initGUI();
                setVisible();
            }
        }));
        putOnBar(new JButton(new AbstractAction("Start Vagrant") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (null != vagrant) {
                    vagrant.stop();
                }
                vagrant = new Vagrant();
                vagrant.start();
            }
        }));
        putOnBar(new JButton(new AbstractAction("Check Vagrant logs") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (null != vagrant) {
                    synchronized (vagrant.log) {
                        String log = vagrant.log.toString();
                        setTabText("Vagrant log", log);
                    }
                }
            }
        }));
        putOnBar(new JButton(new AbstractAction("Stop Vagrant") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (null != vagrant) {
                    vagrant.stop();
                } else {
                    vagrant = new Vagrant();
                    vagrant.stop();
                }
            }
        }));
    }

    private void setTabText(String title, String log) {
        for (int i = 0; i < tabbedPane.getComponentCount(); i++) {
            if (tabbedPane.getTitleAt(i).endsWith(title)) {
                tabbedPane.setComponentAt(i, new JScrollPane(new JTextArea(log)));
                tabbedPane.setSelectedIndex(i);
                return;
            }
        }
        tabbedPane.add(title, new JScrollPane(new JTextArea(log)));
        tabbedPane.setSelectedIndex(tabbedPane.getComponentCount() - 1);
    }

    public final synchronized void putOnBar(Component component) {
        getCompName(component); // test
        delFromBar(component);
        toolBar.add(component);
    }

    private String getCompName(Component component) throws RuntimeException {
        String name = component.getName();
        if (name == null || name.trim().isEmpty()) {
            if (component instanceof JButton) {
                name = ((JButton) component).getText();
            } else if (component instanceof JLabel) {
                name = ((JLabel) component).getText();
            }
        }
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Component name or text must be set and not blank.");
        }
        component.setName(name);
        return name;
    }

    public synchronized void delFromBar(Component component) {
        String name = getCompName(component);
        for (int i = 0; i < toolBar.getComponentCount(); i++) {
            if (toolBar.getComponentAtIndex(i).getName().equals(name)) {
                toolBar.remove(i);
                break;
            }
        }
    }

    public synchronized void setBar(List<Component> components) {
        toolBar.removeAll();
        for (Component component : components) {
            getCompName(component); // to test for proper name
            toolBar.add(component);
        }
    }

    public final void setVisible() {
        if (EventQueue.isDispatchThread()) {
            frame.setVisible(true);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    frame.setVisible(true);
                }
            });
        }
    }

}
