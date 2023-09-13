package nl.infcomtec.llmtools.minigw;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

public class CustomGUI {

    // just for a quick test
    public static void main(String[] args) {
        CustomGUI gui = new CustomGUI();
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
        frame = new JFrame("Custom GUI");
        toolBar = new JToolBar();
        tabbedPane = new JTabbedPane();
        initGUI();
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
        tabbedPane.addTab("Tab 1", new JPanel());
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
