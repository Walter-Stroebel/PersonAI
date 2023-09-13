package nl.infcomtec.llmtools.minigw;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

public class CustomGUI {

    private static Dialogs dialogs;
    private static final Font font = new Font(Font.SERIF, Font.PLAIN, 24); // TODO make adjustable
    private static final String osName = System.getProperty("os.name").toLowerCase();

    // just for a quick test
    public static void main(String[] args) {
        CustomGUI gui = new CustomGUI();
        dialogs = new Dialogs(gui);
        dialogs.addMainTopic(new DialogStep("Test 1", "First test"));
        dialogs.addMainTopic(new DialogStep("Test 2", "Second test"));
        dialogs.addMainTopic(new DialogStep("Test 3", "Third test"));
        dialogs.addMainTopic(new DialogStep("Test 4", "Fourth test"));
        gui.initGUI();
        gui.putOnBar(new JButton(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        }));
        gui.setVisible();
    }

    public final JFrame frame;
    public final JToolBar toolBar;
    public final JTabbedPane tabbedPane;

    public CustomGUI() {
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

        // Add some empty tabs for demonstration
        tabbedPane.addTab("Tab 1", dialogs.getTopics());
        tabbedPane.addTab("Tab 2", new JPanel());

    }

    public synchronized void putOnBar(Component component) {
        String name = getCompName(component);
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
