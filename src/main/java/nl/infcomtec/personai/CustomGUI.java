package nl.infcomtec.personai;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import nl.infcomtec.simpleimage.ImageObject;
import nl.infcomtec.simpleimage.ImageViewer;

public class CustomGUI {

    private static final Font font = new Font(Font.SERIF, Font.PLAIN, 24); // TODO make adjustable
    private static final String osName = System.getProperty("os.name").toLowerCase();

    public final JFrame frame;
    public final JToolBar toolBar;
    public final JTabbedPane tabbedPane;
    public ImageObject dot;
    private JTextArea topic;
    private JList<Instruction> insList;
    private Instructions ins;

    public CustomGUI() {
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
    }

    private void initGUI() {
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
        main.add(new ImageViewer(dot).getScalePanPanel(), BorderLayout.CENTER);
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE, 5), "Chat input"));
        {
            try {
                ins = Instructions.load(new File(MiniGW.WORK_DIR, "instructions.json"), MiniGW.gson);
           // ins.insList.add(new Instruction("Alternatives", "Find any alternatives from this text.", Instruction.ArgumentType.IMMEDIATE_CHILDREN));
            ins.save(new File(MiniGW.WORK_DIR, "instructions.json"), MiniGW.gson);
            } catch (IOException ex) {
                Logger.getLogger(CustomGUI.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            insList = ins.getAsList();
            Box h = Box.createHorizontalBox();
            insList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            h.add(new JLabel("Pick an operation: "));
            h.add(insList);
            box.add(h);
        }
        box.add(Box.createVerticalGlue());
        box.add(new JLabel("Type a message here (optional):"));
        box.add(new JScrollPane(new JTextArea(10, 40)));
        box.add(Box.createVerticalGlue());
        box.add(new JButton(new AbstractAction("\"Ask the chat\"") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                
            }
        }));
        main.add(box, BorderLayout.EAST);
        topic = new JTextArea(20, 80);
        main.add(new JScrollPane(topic), BorderLayout.SOUTH);
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
