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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import nl.infcomtec.graphs.ClEdge;
import nl.infcomtec.graphs.ClNode;
import nl.infcomtec.simpleimage.ImageObject;
import nl.infcomtec.simpleimage.ImageViewer;

/**
 * Main class.
 *
 * @author walter
 */
public class PersonAI {

    public static Font font; /// < This is the base of all scaling code.
    public static final String osName = System.getProperty("os.name").toLowerCase(); /// < Use this if you encounter OS specific issues.
    public static final File HOME_DIR = new File(System.getProperty("user.home"));
    public static final File WORK_DIR = new File(PersonAI.HOME_DIR, ".personAI");
    public static final File LAST_EXIT = new File(PersonAI.WORK_DIR, "LastExit");
    public static final File LAST_CLEAR = new File(PersonAI.WORK_DIR, "LastClear");
    public static final File CONFIG_FILE = new File(PersonAI.WORK_DIR, "config.json");
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create(); /// < A central way to configure GSon.
    public static final File VAGRANT_DIR = new File(PersonAI.HOME_DIR, "vagrant/MiniGW"); /// < This might vary on another OS.
    public static final File VAGRANT_KEY = new File(VAGRANT_DIR, ".vagrant/machines/default/virtualbox/private_key"); /// < This might vary on another OS.
    public final static AtomicInteger totalPromptTokens = new AtomicInteger();
    public final static AtomicInteger totalOutputTokens = new AtomicInteger();
    public final static double ITC = 0.003 / 1000;
    public final static double OTC = 0.004 / 1000;
    private static final String EOLN = System.lineSeparator();
    private static final String ToT_SYSTEM = "You are being used with tree-of-thought tooling. The following are previous messages and an instruction." + EOLN;
    public static final String INS_FILENAME = "instructions.json";
    private static final String GRAPH_TITLE = "Graph";
    private static final String LAST_TITLE = "Last Interaction";
    private Component graph;
    private Component last;
    public static Config config;
    public static final Random random = new Random();
    public static final int loMark = Integer.parseInt("1000", 36);
    public static final int hiMark = Integer.parseInt("zzzz", 36);

    public static String getUniqueMark(String text) {
        while (true) {
            int mark = random.nextInt(hiMark - loMark + 1) + loMark;
            String ret = Integer.toString(mark, 36);
            if (!text.contains(ret)) {
                return ret;
            }
        }
    }

    /**
     * Starting point.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        doConfig();
        new PersonAI();
    }

    private static void doConfig() {
        GraphicsEnvironment grEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = grEnv.getDefaultScreenDevice();
        DisplayMode displayMode = defaultScreen.getDisplayMode();
        WORK_DIR.mkdirs(); // in case it doesn't
        if (!WORK_DIR.exists()) {
            System.err.println("Cannot access nor create work directory? " + WORK_DIR);
            System.exit(1);
        }
        if (CONFIG_FILE.exists()) {
            try ( FileReader fr = new FileReader(CONFIG_FILE)) {
                config = gson.fromJson(fr, Config.class);
            } catch (Exception any) {
                config = null;
            }
        }
        if (null == config) {
            config = new Config();
            config.darkMode = true;
            font = new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(displayMode.getHeight() / 90, 11));
            config.fontName = font.getFontName();
            config.fontSize = font.getSize();
            config.fontStyle = font.getStyle();
            config.w20Per = displayMode.getWidth() * 20 / 100;
            config.w20PerDeco = displayMode.getWidth() * 20 / 100 - 35;
            config.h20Per=displayMode.getHeight() * 20 / 100;
            config.hFull=displayMode.getHeight();
            
            saveConfig();
        } else {
            font = config.getFont();
        }
    }

    public static void saveConfig() {
        try ( FileWriter fw = new FileWriter(CONFIG_FILE)) {
            gson.toJson(config, fw);
            fw.write(EOLN);
        } catch (IOException ex) {
            System.err.println("Cannot write to work directory? " + WORK_DIR + " " + ex.getMessage());
            System.exit(1);
        }
    }
    public ImageObject dot;
    public JFrame frame;
    public JToolBar toolBar;
    public JTabbedPane tabbedPane;
    private Instructions ins;
    private Instruction curIns;
    public Conversation convo = new Conversation();
    private ImageViewer dotViewer;
    private Vagrant vagrant;
    private JLabel costLabel;
    private JTextArea progressMsg;
    private ButtonGroup insGrp;
    private final JProgressBar working = new JProgressBar();
    private long workingStart;

    public PersonAI() throws IOException {
        initGUI();
        setVisible();
    }

    private void setUI() {
        Set<Map.Entry<Object, Object>> entries = new HashSet(UIManager.getLookAndFeelDefaults().entrySet());
        for (Map.Entry<Object, Object> entry : entries) {
            if (entry.getValue() instanceof Font) {
                UIManager.put(entry.getKey(), font);
            } else if (entry.getValue() instanceof Color) {
                UIManager.put(entry.getKey(), config.mapTo((Color) entry.getValue()));
            }
        }
        /* TODO create a settings thingy
        final JDialog editColors = config.editColors();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                editColors.setVisible(true);
            }
        });
         */
        saveConfig();
    }

    private void initGUI() {
        try {
            dot = new ImageObject(ImageIO.read(getClass().getResourceAsStream("/robotHelper.png")));
        } catch (IOException ex) {
            Logger.getLogger(PersonAI.class.getName()).log(Level.SEVERE, null, ex);
        }
        setUI();
        frame = new JFrame("PersonAI");
        toolBar = new JToolBar();
        tabbedPane = new JTabbedPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Add JToolBar to the north
        frame.getContentPane().add(toolBar, BorderLayout.NORTH);
        // Add JTabbedPane to the center
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        JPanel grPane = new JPanel(new BorderLayout());
        dotViewer = new ImageViewer(dot);
        JPanel viewPanel = dotViewer.getScalePanPanel();
        dot.addListener(new GraphMouse("Mouse"));
        grPane.add(viewPanel, BorderLayout.CENTER);
        JButton submit = new JButton(new SubmitAction("Send to AI/LLM")); // TODO
        ClNode start = convo.newNode("Start", "box", "");
        addReplaceTab(start).setEditMode(true);
        tabbedPane.addTab(GRAPH_TITLE, grPane);
        addButtons();
    }

    public Component createInsPanel() {
        JPanel insPan = new JPanel(new FlowLayout());
        ins = Instructions.load(new File(WORK_DIR, INS_FILENAME), gson);
        insPan.add(new JLabel("Pick an operation: "));
        insGrp = new ButtonGroup();
        for (final Instruction i : ins.insList) {
            JCheckBox jb = new JCheckBox(new AbstractAction(i.description) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (!convo.hasSelection()) {
                        JOptionPane.showMessageDialog(frame, "Please select a node first.");
                        return;
                    }
                    curIns = i;
                }
            });
            jb.setToolTipText(i.prompt);
            insPan.add(jb);
            insGrp.add(jb);
        }
        insPan.setPreferredSize(new Dimension(config.w20PerDeco, config.hFull));
        JScrollPane ret = new JScrollPane(insPan);
        ret.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.BLUE, 5), "Suggestions"));
        ret.setPreferredSize(new Dimension(config.w20Per, config.h20Per));
        return ret;
    }

    public JPanel interactionPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setPreferredSize(new Dimension(config.w20Per, config.h20Per));
        statusPanel.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.BLUE, 5), "Status"));
        progressMsg = new JTextArea("");
        costLabel = new JLabel("Cost: $0.00");
        statusPanel.add(new JScrollPane(progressMsg), BorderLayout.CENTER);
        statusPanel.add(costLabel, BorderLayout.SOUTH);
        return statusPanel;
    }

    private void addButtons() {
        JButton green = new JButton(new AbstractAction("Welcome!") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                convo.save(LAST_CLEAR, gson);
                convo.clear();
                ClNode start = convo.newNode("Start", "box", getResource("start.md"));
                addReplaceTab(start);
                rebuildLast();
            }
        });
        green.setForeground(Color.GREEN.darker());
        putOnBar(green);
        putOnBar(new JButton(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                convo.save(LAST_EXIT, gson);
                System.exit(0);
            }
        }));
        putOnBar(new JButton(new AbstractAction("Bigger text") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                font = new Font(font.getName(), font.getStyle(), font.getSize() + 1);
                initGUI();
                rebuild();
                setVisible();
            }
        }));
        putOnBar(new JButton(new AbstractAction("Smaller text") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (font.getSize() > 6) {
                    font = new Font(font.getName(), font.getStyle(), font.getSize() - 1);
                    initGUI();
                    rebuild();
                    setVisible();
                }
            }
        }));
        putOnBar(new JButton(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JFileChooser jfc = new JFileChooser(WORK_DIR);
                int ret = jfc.showSaveDialog(frame);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    convo.save(jfc.getSelectedFile(), gson);
                }
            }
        }));
        putOnBar(new JButton(new AbstractAction("Load") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JFileChooser jfc = new JFileChooser(WORK_DIR);
                int ret = jfc.showOpenDialog(frame);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    convo.loadConvo(jfc.getSelectedFile(), gson);
                    rebuild();
                }
            }
        }));
        putOnBar(new JButton(new AbstractAction("Clear All") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                convo.save(LAST_CLEAR, gson);
                convo.clear();
                rebuild();
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
        working.setName("prog");
        putOnBar(working);
    }

    public static String getResource(String name) {
        String path = name.startsWith("/") ? name : "/" + name;
        try ( BufferedReader bfr = new BufferedReader(new InputStreamReader(PersonAI.class.getResourceAsStream(path)))) {
            StringBuilder sb = new StringBuilder();
            for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                sb.append(s).append(EOLN);
            }
            return sb.toString();
        } catch (Exception ex) {
            return "Reading from resource failed: " + ex.getMessage();
        }
    }

    /**
     * Rebuild all.
     */
    private void rebuild() {
        workingStart = System.nanoTime();
        working.setIndeterminate(true);
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                doRebuild();
                return null;
            }
        };
        worker.execute();
    }

    /**
     * Rebuild all.
     */
    private void rebuildLast() {
        workingStart = System.nanoTime();
        working.setIndeterminate(true);
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                doRebuild();
                tabbedPane.setSelectedIndex(tabbedPane.getComponentCount() - 1);
                return null;
            }
        };
        worker.execute();

    }

    private void doRebuild() {
        try {
            ins = Instructions.load(new File(PersonAI.WORK_DIR, INS_FILENAME), gson);
            BufferedImage render = convo.render();
            dot.putImage(render);
            convo.segments = dot.calculateClosestAreas(convo.nodeCenters);
            for (int i = 0; i < tabbedPane.getComponentCount(); i++) {
                String tit = tabbedPane.getTitleAt(i);
                if (tit.equals(LAST_TITLE)) {
                    last = tabbedPane.getComponentAt(i);
                }
                if (tit.equals(GRAPH_TITLE)) {
                    graph = tabbedPane.getComponentAt(i);
                }
            }
            tabbedPane.removeAll();
            if (null != graph) {
                tabbedPane.add(GRAPH_TITLE, graph);
            }
            if (null != last) {
                tabbedPane.add(LAST_TITLE, last);
            }
            for (ClNode n : convo.getNodes()) {
                addReplaceTab(n);
            }
            frame.repaint();
        } catch (Exception ex) {
            Logger.getLogger(PersonAI.class.getName()).log(Level.SEVERE, null, ex);
        }
        working.setIndeterminate(false);//.running.set(false);
        working.setStringPainted(true);
        working.setString(String.format("%.3f s", (System.nanoTime() - workingStart) / 1E9));
    }

    private void setTabText(String title, String text) {
        JScrollPane pane = new JScrollPane(new JTextArea(text));
        setTab(title, pane);
    }

    private void closeTab(ClNode node) {
        String t = node.getName() + "." + node.label;
        for (int i = 0; i < tabbedPane.getComponentCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(t)) {
                tabbedPane.remove(i);
                return;
            }
        }
    }

    public void setTab(String title, Component comp) {
        for (int i = 0; i < tabbedPane.getComponentCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(title)) {
                System.out.println("Dup? " + title);
                tabbedPane.setComponentAt(i, comp);
                return;
            }
        }
        tabbedPane.add(title, comp);
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

    private NodePanel addReplaceTab(ClNode node) {
        NodePanel panel = new NodePanel(this, node, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                frame.repaint();
            }
        }, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                rebuild();
            }
        });
        return panel;
    }

    public void splitNode(ClNode node, int rClickPos) {
        String t = Conversation.getText(node);
        String t1 = t.substring(0, rClickPos).trim();
        String t2 = t.substring(rClickPos, t.length()).trim();
        Conversation.setText(node, t1);
        ClNode n2 = convo.newNode(node.label + " 2", node.getShape(), t2);
        convo.addEdge(node, n2, "split");
        rebuild();
    }

    public Component graphPanel(ClNode ref) {
        JPanel links = new JPanel(new GridLayout(0, 4));
        links.setPreferredSize(new Dimension(config.w20PerDeco, config.h20Per));
        links.add(new JLabel("Link"));
        links.add(new JLabel("Node"));
        links.add(new JLabel("Delete link"));
        links.add(new JLabel("Delete node"));
        for (ClEdge e : convo.getEdges()) {
            if (e.fromNode.equals(ref)) {
                links.add(new JLabel(e.label));
                links.add(new JLabel(e.fromNode.label));
                links.add(new JButton(new AbstractAction("From") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                    }
                }));
                links.add(new JButton(new AbstractAction("?") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                    }
                }));
            }
            if (e.toNode.equals(ref)) {
                links.add(new JLabel(e.label));
                links.add(new JLabel(e.toNode.label));
                links.add(new JButton(new AbstractAction("To") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                    }
                }));
                links.add(new JButton(new AbstractAction("?") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                    }
                }));
            }
        }
        JScrollPane ret = new JScrollPane(links);
        ret.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.GREEN, 5), "Links"));
        ret.setPreferredSize(new Dimension(config.w20Per, config.h20Per));
        return ret;
    }

    private class GraphMouse extends ImageObject.ImageObjectListener {

        public GraphMouse(String name) {
            super(name);
        }

        @Override
        public void mouseEvent(ImageObject imgObj, ImageObject.MouseEvents ev, MouseEvent e) {
            ClNode node = convo.getNode(e);
            if (SwingUtilities.isLeftMouseButton(e)) {
                convo.selectNode(node, dotViewer);
            } else {
                convo.unselectNode(node, dotViewer);
            }
            // TODO topic.setText(convo.getSelNodeText());
            frame.repaint();
        }
    }

    private class SubmitAction extends AbstractAction {

        public SubmitAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            StringBuilder sb = convo.getSelectedText("");
            String question = sb.toString().trim();
            if ((!question.isEmpty())) {
                workingStart = System.nanoTime();
                working.setIndeterminate(true);
                progressMsg.setText("");
                ApiRequestWorker worker = new ApiRequestWorker(question);
                worker.execute();
            }
        }
    }

    private class ApiRequestWorker extends SwingWorker<String, String> {

        private final String question;
        private Exception failure;
        private final StringBuilder lastInteraction = new StringBuilder();

        public ApiRequestWorker(String question) {
            this.question = question;
        }

        @Override
        protected String doInBackground() {
            publish("1:Sending question to LLM");
            try {
                String answer;
                if (null != curIns) {
                    synchronized (lastInteraction) {
                        lastInteraction.append("SYSTEM: ").append(ToT_SYSTEM).append(EOLN);
                        lastInteraction.append("QUESTION: ").append(curIns.prompt).append(EOLN);
                        lastInteraction.append("TEXT: ").append(question).append(EOLN);
                    }
                    answer = OpenAIAPI.makeRequest(new Message[]{
                        new Message(Message.ROLES.system, ToT_SYSTEM),
                        new Message(Message.ROLES.user, curIns.prompt),
                        new Message(Message.ROLES.assistant, question)
                    });
                } else {
                    lastInteraction.append("TEXT: ").append(question).append(EOLN);
                    answer = OpenAIAPI.makeRequest(null, question);
                }
                synchronized (lastInteraction) {
                    lastInteraction.append(answer).append(EOLN);
                }
                publish("2:Asking LLM for tagline");
                String tagLine = OpenAIAPI.makeRequest("Give me a short tagline of at most 20 characters.", answer);
                convo.clearSelection();
                insGrp.clearSelection();
                // XXX cost calculation
                for (Iterator<Usage> it = OpenAIAPI.usages.iterator(); it.hasNext();) {
                    Usage us = it.next();
                    it.remove();
                    totalPromptTokens.addAndGet(us.promptTokens);
                    totalOutputTokens.addAndGet(us.completionTokens);
                }
                System.out.format("Cost: %.2f + %.2f = %3$.2f (%3$f)\n",
                        totalPromptTokens.get() * ITC,
                        totalOutputTokens.get() * OTC,
                        totalPromptTokens.get() * ITC + totalOutputTokens.get() * OTC);
                double cost = totalPromptTokens.get() * ITC + totalOutputTokens.get() * OTC;
                costLabel.setText(String.format("Cost: $%.2f", cost));
                synchronized (lastInteraction) {
                    lastInteraction.append("Tagline: ").append(tagLine).append(EOLN);
                }
                publish("3:Rebuilding graph");
                ClNode q = convo.newNode(null != curIns ? curIns.description : "Question", "diamond", question);
                convo.addAnswer(q, tagLine, answer);
                // TODO topic.setText(new PandocConverter().convertMarkdownToText(answer));
                doRebuild();
                publish("0:Ready for next question");
            } catch (Exception e) {
                this.failure = e;
            }

            return "";
        }

        @Override
        protected void process(List<String> chunks) {
            synchronized (lastInteraction) {
                setTabText(LAST_TITLE, lastInteraction.toString());
                tabbedPane.setSelectedIndex(0);
            }
            for (String msg : chunks) {
                if (msg.indexOf(':') == 1) {
                    progressMsg.append(EOLN);
                    progressMsg.append(msg.substring(2));
                } else {
                    progressMsg.append(EOLN);
                    progressMsg.append(msg);
                }
            }
        }

        @Override
        protected void done() {
            if (failure != null) {
                // If an exception occurred, update the status message
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        progressMsg.setText(failure.getMessage());
                    }
                });
//                Logger.getLogger(PersonAI.class.getName()).log(Level.SEVERE, null, failure);
            } else {
                // Handle successful completion and update the UI accordingly
            }
        }
    }

}
