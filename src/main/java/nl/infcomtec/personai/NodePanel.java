package nl.infcomtec.personai;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import nl.infcomtec.graphs.ClNode;
import nl.infcomtec.tools.PandocConverter;

/**
 * Show a node in full detail with some editing options.
 *
 * @author Walter Stroebel
 */
public class NodePanel extends JPanel {

    private final static AtomicReference<Font> dFont = new AtomicReference<>(PersonAI.font);
    private final ClNode node;
    private final Action repAct;
    private final Action rebuild;
    private final PersonAI owner;
    private final JButton btApply;
    private final JComboBox<String> cbShapes;
    private final JEditorPane editorPane;
    private final JPanel sidePanel;
    private final JScrollPane mainScrollPane;
    private final JCheckBox btEdit;
    private final JSlider fontSizer;
    private final JTextField tfLabel;
    private boolean html = true;
    private JButton btReset;
    private JButton btDelete;
    private JButton btSplit;

    /**
     * Creates new form NodePanel
     *
     * @param owner Owning object.
     * @param cvNode Clickable node.
     * @param repAct call this to repaint.
     * @param rebuild call this to rebuild.
     */
    public NodePanel(PersonAI owner, ClNode cvNode, Action repAct, Action rebuild) {
        this.btApply = new JButton();
        this.cbShapes = new JComboBox<>();
        this.editorPane = new JEditorPane();
        this.sidePanel = new JPanel();
        this.mainScrollPane = new JScrollPane();
        this.fontSizer = new JSlider();
        this.tfLabel = new JTextField();
        this.node = cvNode;
        this.btEdit = new JCheckBox(new AbstractAction("Edit the text") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                setEditMode(html);
            }
        });
        this.owner = owner;
        this.repAct = repAct;
        this.rebuild = rebuild;
        initComponents();
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setText(Conversation.getHTML(node));
        cbShapes.setSelectedItem(cvNode.getShape());
        owner.setTab(node.getName() + "." + node.label, this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        setLayout(new java.awt.BorderLayout());
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setFont(dFont.get());
        mainScrollPane.setViewportView(editorPane);
        add(mainScrollPane, java.awt.BorderLayout.CENTER);
        sidePanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = GridBagConstraints.WEST;
        tfLabel.setColumns(20);
        tfLabel.setText(node.label);
        enclosed(gc, "Display label", sidePanel, tfLabel);
        enclosed(gc, "Edit the main text", sidePanel, btEdit);
        fontSizer.setMaximum(48);
        fontSizer.setMinimum(6);
        fontSizer.setToolTipText("Change the size of the displayed text.");
        fontSizer.setValue(24);
        fontSizer.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                fontSizeChanged(evt);
            }
        });
        enclosed(gc, "Font size", sidePanel, fontSizer);
        cbShapes.setEditable(true);
        cbShapes.setModel(new DefaultComboBoxModel<>(new String[]{"ellipse", "box", "circle", "diamond", "rectangle", "plaintext", "triangle", "hexagon", "octagon", "parallelogram"}));
        enclosed(gc, "Shape", sidePanel, cbShapes);
        btApply.setText("Apply");
        btApply.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                node.label = tfLabel.getText();
                node.setShape(cbShapes.getSelectedItem().toString());
                if (html) {
                    Conversation.setHTML(node, editorPane.getText());
                } else {
                    Conversation.setText(node, editorPane.getText());
                }
                rebuild.actionPerformed(null);
            }
        });
        btReset = new JButton(new AbstractAction("Clear") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                editorPane.setContentType("text/plain");
                editorPane.setText("");
                editorPane.setEditable(true);
                btEdit.setSelected(true);
            }
        });
        btDelete = new JButton(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                owner.convo.delete(node);
            }
        });
        btSplit = new JButton(new AbstractAction("Split") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                StringBuilder text = new StringBuilder(editorPane.getText());
                String sel = editorPane.getSelectedText();
                if (null == sel || sel.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "You need to select something to split?");
                } else {
                    int startPos = text.indexOf(sel);
                    if (startPos < 0) {
                        System.out.println("Text=[" + text + "]\nSel=[" + sel + "]\nStart=" + startPos);
                        return;
                    }
                    int endPos = startPos + sel.length();
                    text.delete(startPos, endPos);
                    String left = text.toString();
                    System.out.println("Sel=[" + sel + "]\nLeft=[" + left + "]\nStart/Stop=" + startPos + "/" + endPos);
                    if (left.trim().isEmpty()) {
                        sel = sel.trim();
                        editorPane.setText(sel);
                        JOptionPane.showMessageDialog(null, "There must be something left after the split.");
                    } else {
                        sel = sel.trim() + System.lineSeparator();
                        left = left.trim() + System.lineSeparator();
                        owner.splitNode(node, left.trim(), sel);
                    }
                }
            }
        });
        btSplit.setEnabled(!html);
        enclosed(gc, "Actions", sidePanel, btApply, btReset, btDelete, btSplit);
        sidePanel.add(Box.createVerticalGlue());
        gc.gridwidth = 2;
        sidePanel.add(owner.graphPanel(node), gc);
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

    private void fontSizeChanged(ChangeEvent evt) {
        int s = fontSizer.getValue();
        if (s != dFont.get().getSize()) {
            dFont.set(new Font(dFont.get().getFontName(), dFont.get().getStyle(), s));
            editorPane.setFont(dFont.get());
            repAct.actionPerformed(null);
        }
    }

    public Component setEditMode(boolean b) {
        if (b && html) {
            String tt = new PandocConverter().convertHTMLToMarkdown(editorPane.getText());
            editorPane.setContentType("text/plain");
            editorPane.setText(tt);
            editorPane.setEditable(true);
            btSplit.setEnabled(true);
            html = false;
        } else if (!html) {
            String ht = new PandocConverter().convertMarkdownToHTML(editorPane.getText());
            editorPane.setContentType("text/html");
            editorPane.setText(ht);
            editorPane.setEditable(false);
            btSplit.setEnabled(false);
            html = true;
        }
        btEdit.setSelected(b);
        return this;
    }
}
