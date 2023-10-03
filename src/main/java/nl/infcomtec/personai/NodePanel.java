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
    private final JPanel jPanel1;
    private final JScrollPane jScrollPane1;
    private final JCheckBox btEdit;
    private final JSlider jSlider1;
    private final JTextField tfLabel;

    /**
     * Creates new form NodePanel
     *
     * @param owner Owning object.
     * @param node Clickable node.
     * @param repAct call this to repaint.
     * @param rebuild call this to rebuild.
     */
    public NodePanel(PersonAI owner, ClNode node, Action repAct, Action rebuild) {
        this.btApply = new JButton();
        this.cbShapes = new JComboBox<>();
        this.editorPane = new JEditorPane();
        this.jPanel1 = new JPanel();
        this.jScrollPane1 = new JScrollPane();
        this.jSlider1 = new JSlider();
        this.tfLabel = new JTextField();
        this.btEdit = new JCheckBox(new AbstractAction("Edit the text") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String text = editorPane.getText();
                if (text.startsWith("<html>")) {
                    editorPane.setContentType("text/plain");
                    editorPane.setText(new PandocConverter().convertHTMLToMarkdown(text));
                    editorPane.setEditable(true);
                } else {
                    editorPane.setContentType("text/html");
                    editorPane.setText(new PandocConverter().convertMarkdownToHTML(text));
                    editorPane.setEditable(false);
                }
            }
        });
        this.owner = owner;
        this.node = node;
        this.repAct = repAct;
        this.rebuild = rebuild;
        initComponents();
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setText(new PandocConverter().convertMarkdownToHTML(node.getUserStr()));
        cbShapes.setSelectedItem(node.getShape());
        owner.setTab(node.getName() + "." + node.label, this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        setLayout(new java.awt.BorderLayout());

        editorPane.setEditable(false);
        if (editorPane.isEditable()) {
            throw new RuntimeException("No!");
        }
        editorPane.setContentType("text/html");
        editorPane.setFont(dFont.get());
        jScrollPane1.setViewportView(editorPane);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;
        tfLabel.setColumns(20);
        tfLabel.setText(node.label);
        enclosed(gc, "Display label", jPanel1, tfLabel);
        enclosed(gc, "Edit the main text", jPanel1, btEdit);
        jSlider1.setMaximum(48);
        jSlider1.setMinimum(6);
        jSlider1.setToolTipText("Change the size of the displayed text.");
        jSlider1.setValue(24);

        jSlider1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });
        enclosed(gc, "Font size", jPanel1, jSlider1);

        cbShapes.setEditable(true);
        cbShapes.setModel(new DefaultComboBoxModel<>(new String[]{"ellipse", "box", "circle", "diamond", "rectangle", "plaintext", "triangle", "hexagon", "octagon", "parallelogram"}));
        enclosed(gc, "Shape", jPanel1, cbShapes);

        btApply.setText("Apply");
        btApply.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btApplyActionPerformed(evt);
            }
        });
        JButton btReset = new JButton(new AbstractAction("Clear") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                editorPane.setContentType("text/plain");
                editorPane.setText("");
                editorPane.setEditable(true);
                btEdit.setSelected(true);
            }
        });
        enclosed(gc, "Actions", jPanel1, btApply, btReset);
        jPanel1.add(Box.createVerticalGlue());

        add(jPanel1, java.awt.BorderLayout.EAST);
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

    private void btApplyActionPerformed(java.awt.event.ActionEvent evt) {
        node.label = tfLabel.getText();
        node.setShape(cbShapes.getSelectedItem().toString());
        String text = editorPane.getText();
        if (text.startsWith("<html>")) {
            node.setUserObj(new PandocConverter().convertHTMLToMarkdown(text));
        } else {
            node.setUserObj(text);
        }
        rebuild.actionPerformed(null);
    }

    private void jSlider1StateChanged(ChangeEvent evt) {
        int s = jSlider1.getValue();
        if (s != dFont.get().getSize()) {
            dFont.set(new Font(dFont.get().getFontName(), dFont.get().getStyle(), s));
            editorPane.setFont(dFont.get());
            repAct.actionPerformed(null);
        }
    }
}
