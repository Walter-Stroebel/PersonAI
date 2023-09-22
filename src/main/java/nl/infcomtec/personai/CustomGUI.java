package nl.infcomtec.personai;

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

public class CustomGUI {

    public static final Font font = new Font(Font.SERIF, Font.PLAIN, 24); // TODO make adjustable
    public static final String osName = System.getProperty("os.name").toLowerCase();
    public final JFrame frame;
    public final JToolBar toolBar;
    public final JTabbedPane tabbedPane;
    public ImageObject dot;
    private JTextArea topic;
    private Instructions ins;
    private Instruction curIns;
    public ClGraph graph = new ClGraph();

    public CustomGUI() throws IOException {
        try {
            dot = new ImageObject(ImageIO.read(getClass().getResourceAsStream("/robotHelper.png")));
        } catch (IOException ex) {
            Logger.getLogger(CustomGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        try ( BufferedReader bfr = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/uimanager.fontKeys")))) {
            for (String key = bfr.readLine(); null != key; key = bfr.readLine()) {
                UIManager.put(key, font);
            }
        } catch (IOException ex) {
            // we tried ... might not be fatal
            System.err.println(ex.getMessage());
        }
        frame = new JFrame("Custom GUI");
        toolBar = new JToolBar();
        tabbedPane = new JTabbedPane();
        initGUI();
        putOnBar(new JButton(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        }));
        putOnBar(new JButton(new AbstractAction("Load") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    ins = Instructions.load(new File(MiniGW.WORK_DIR, "instructions.json"), MiniGW.gson);
                    graph.clear();
                    ClNode n1 = graph.addNode(new ClNode(graph, UUID.randomUUID().toString(), "shape=box"));
                    ClNode n2 = graph.addNode(new ClNode(graph, UUID.randomUUID().toString(), "shape=diamond"));
                    ClNode n3 = graph.addNode(new ClNode(graph, UUID.randomUUID().toString(), "shape=ellipse"));
                    graph.addNode(new ClEdge(n1, n2, "", null));
                    graph.addNode(new ClEdge(n2, n3, "", null));
                    graph.selection.add(n2.id);
                    graph.selection.add(n3.id);
                    BufferedImage render = graph.render();
                    dot.putImage(render);
                    frame.repaint();
                } catch (Exception ex) {
                    Logger.getLogger(CustomGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
        putOnBar(new JButton(new AbstractAction("Clear") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                frame.repaint();
            }
        }));
    }

    private void initGUI() throws IOException {
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
        JPanel dotViewer = new ImageViewer(dot).getScalePanPanel();
        dot.addListener(new ImageObject.ImageObjectListener("Mouse") {
            @Override
            public void mouseEvent(ImageObject imgObj, ImageObject.MouseEvents ev, MouseEvent e) {
                System.out.println(graph.getNode(e).label);
            }
        });
        main.add(dotViewer, BorderLayout.CENTER);
        {
            JPanel box = new JPanel(new BorderLayout());
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            DisplayMode dm = defaultScreen.getDisplayMode();
            box.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            box.setPreferredSize(new Dimension(dm.getWidth() / 4, dm.getHeight() * 70 / 100));
            JPanel pan = new JPanel(new FlowLayout());
            JScrollPane inpPan = new JScrollPane(new JTextArea());
            inpPan.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE, 5), "Type a message here (optional):"));
            topic = new JTextArea();
            JScrollPane topPan = new JScrollPane(topic);
            topPan.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE, 5), "Selected node:"));
            topPan.setPreferredSize(new Dimension(dm.getWidth(), dm.getHeight() * 30 / 100));
            ins = Instructions.load(new File(MiniGW.WORK_DIR, "instructions.json"), MiniGW.gson);
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
            box.add(new JButton(new AbstractAction("Ask the chat") {
                @Override
                public void actionPerformed(ActionEvent ae) {

                }
            }), BorderLayout.SOUTH);
            main.add(box, BorderLayout.EAST);
            main.add(topPan, BorderLayout.SOUTH);
        }
        tabbedPane.addTab("Main", main);
        tabbedPane.addTab("Tab 2", new JPanel());
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

    public void setVisible() {
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
