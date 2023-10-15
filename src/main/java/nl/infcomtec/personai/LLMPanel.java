package nl.infcomtec.personai;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * Show a node in full detail with some editing options.
 *
 * @author Walter Stroebel
 */
public class LLMPanel extends JPanel {

    private final Action repAct;
    private final Action rebuild;
    private final PersonAI owner;
    private final JEditorPane question;
    private final JPanel sidePanel;
    private String wholeText;
    private int selectionStart;
    private int selectionEnd;
    private String textBeforeSelection;
    private String selectedText;
    private String textAfterSelection;
    private final JEditorPane system;
    private final JEditorPane text;

    /**
     * Creates new form LLMPanel
     *
     * @param owner Owning object.
     * @param repAct call this to repaint.
     * @param rebuild call this to rebuild.
     */
    public LLMPanel(PersonAI owner, Action repAct, Action rebuild) {
        this.system = new JEditorPane();
        this.question = new JEditorPane();
        this.text = new JEditorPane();
        this.sidePanel = new JPanel();
        this.owner = owner;
        this.repAct = repAct;
        this.rebuild = rebuild;
        initComponents();
        question.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem breakNodeItem = new JMenuItem(new AbstractAction("Something1") {
            @Override
            public void actionPerformed(ActionEvent ae) {
            }
        });
        popupMenu.add(breakNodeItem);
        setLayout(new GridBagLayout());
        GridBagConstraints mainGBC = new GridBagConstraints();
        mainGBC.fill = GridBagConstraints.BOTH;
        mainGBC.weightx = 0.8;
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Get the whole text
                    wholeText = question.getText();

                    // Get the start and end indices of the selected text
                    selectionStart = question.getSelectionStart();
                    selectionEnd = question.getSelectionEnd();

                    // Extract the parts of the text
                    textBeforeSelection = wholeText.substring(0, selectionStart);
                    selectedText = question.getSelectedText();
                    textAfterSelection = wholeText.substring(selectionEnd);
                    popupMenu.show(question, e.getX(), e.getY());
                }
            }
        };
        mainGBC.gridx = 0;
        mainGBC.gridy = 0;
        mainGBC.weighty = 0.2;
        add(titledScrollPane("System", system, ma), mainGBC);
        mainGBC.gridx = 0;
        mainGBC.gridy = 1;
        mainGBC.weighty = 0.6;
        add(titledScrollPane("Text", text, ma), mainGBC);
        mainGBC.gridx = 0;
        mainGBC.gridy = 2;
        mainGBC.weighty = 0.2;
        add(titledScrollPane("Question", question, ma), mainGBC);
        system.setEditable(true);
        system.setContentType("text/plain");
        system.addMouseListener(ma);
        system.setText(PersonAI.ToT_SYSTEM);
        sidePanel.setLayout(new GridBagLayout());
        GridBagConstraints sideGBC = new GridBagConstraints();
        sideGBC.gridx = 0;
        sideGBC.gridy = 0;
        sideGBC.insets = new Insets(5, 5, 5, 5);
        sideGBC.anchor = GridBagConstraints.WEST;
        JButton btReset = new JButton(new AbstractAction("Clear") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                system.setContentType("text/plain");
                system.setText(PersonAI.ToT_SYSTEM);
                system.setEditable(true);
                question.setContentType("text/plain");
                question.setText("");
                question.setEditable(true);
                text.setContentType("text/plain");
                text.setText("");
                text.setEditable(true);
            }
        });
        sideGBC.gridwidth = 1;
        sidePanel.add(owner.createInsPanel(question), sideGBC);
        sideGBC.gridy++;
        sidePanel.add(owner.interactionPanel(), sideGBC);
        sideGBC.gridy++;
        enclosed(sideGBC, "Actions", sidePanel, owner.aiSubmit(system, question, text), btReset);
        mainGBC.gridx = 1;
        mainGBC.gridy = 0;
        mainGBC.gridheight = 3;
        mainGBC.weightx = 0.2;
        add(sidePanel, mainGBC);
    }

    private JScrollPane titledScrollPane(String title, JEditorPane comp, MouseAdapter ma) {
        comp.setEditable(true);
        comp.setContentType("text/plain");
        comp.addMouseListener(ma);
        JScrollPane ret = new JScrollPane(comp);
        ret.setMinimumSize(new Dimension(600, PersonAI.config.h20Per));
        Border line = BorderFactory.createLineBorder(Color.BLUE);
        ret.setBorder(BorderFactory.createTitledBorder(line, title));
        return ret;
    }

    private void enclosed(GridBagConstraints gc, String title, JPanel pn, Component... comp) {
        JPanel ip = new JPanel(new FlowLayout());
        ip.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.BLUE, 5), title));
        for (Component c : comp) {
            ip.add(c, gc);
        }
        ip.setPreferredSize(new Dimension(PersonAI.config.w20Per, 100));
        pn.add(ip, gc);
        gc.gridy++;
    }

}
