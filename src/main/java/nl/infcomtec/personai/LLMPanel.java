package nl.infcomtec.personai;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Show a node in full detail with some editing options.
 *
 * @author Walter Stroebel
 */
public class LLMPanel extends JPanel {

    private final Action repAct;
    private final Action rebuild;
    private final PersonAI owner;
    private final JButton btApply;
    private final JEditorPane editorPane;
    private final JPanel sidePanel;
    private final JScrollPane jScrollPane1;
    private String wholeText;
    private int selectionStart;
    private int selectionEnd;
    private String textBeforeSelection;
    private String selectedText;
    private String textAfterSelection;

    /**
     * Creates new form LLMPanel
     *
     * @param owner Owning object.
     * @param repAct call this to repaint.
     * @param rebuild call this to rebuild.
     */
    public LLMPanel(PersonAI owner, Action repAct, Action rebuild) {
        this.btApply = new JButton();
        this.editorPane = new JEditorPane();
        this.sidePanel = new JPanel();
        this.jScrollPane1 = new JScrollPane();
        this.owner = owner;
        this.repAct = repAct;
        this.rebuild = rebuild;
        initComponents();
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        setLayout(new java.awt.BorderLayout());
        editorPane.setEditable(false);
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem breakNodeItem = new JMenuItem(new AbstractAction("Something1") {
            @Override
            public void actionPerformed(ActionEvent ae) {
            }
        });

        popupMenu.add(breakNodeItem);

        editorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Get the whole text
                    wholeText = editorPane.getText();

                    // Get the start and end indices of the selected text
                    selectionStart = editorPane.getSelectionStart();
                    selectionEnd = editorPane.getSelectionEnd();

                    // Extract the parts of the text
                    textBeforeSelection = wholeText.substring(0, selectionStart);
                    selectedText = editorPane.getSelectedText();
                    textAfterSelection = wholeText.substring(selectionEnd);
                    popupMenu.show(editorPane, e.getX(), e.getY());
                }
            }
        });
        if (editorPane.isEditable()) {
            throw new RuntimeException("No!");
        }
        editorPane.setContentType("text/html");
        jScrollPane1.setViewportView(editorPane);
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
        sidePanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = GridBagConstraints.WEST;
        btApply.setText("Apply");
        btApply.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rebuild.actionPerformed(null);
            }
        });
        JButton btReset = new JButton(new AbstractAction("Clear") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                editorPane.setContentType("text/plain");
                editorPane.setText("");
                editorPane.setEditable(true);
            }
        });
        enclosed(gc, "Actions", sidePanel, btApply, btReset);
        sidePanel.add(Box.createVerticalGlue());
        gc.gridwidth = 2;
        sidePanel.add(owner.createInsPanel(), gc);
        gc.gridy++;
        sidePanel.add(owner.interactionPanel(), gc);
        add(sidePanel, java.awt.BorderLayout.EAST);
    }

    private void enclosed(GridBagConstraints gc, String title, JPanel pn, Component... comp) {
        JLabel t = new JLabel(title);
        pn.add(t, gc);
        JPanel ip = new JPanel(new FlowLayout());
        for (Component c : comp) {
            ip.add(c, gc);
        }
        gc.gridx = 1;
        pn.add(ip, gc);
        gc.gridx = 0;
        gc.gridy++;
    }

}
