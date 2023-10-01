package nl.infcomtec.personai;

import java.awt.Font;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JEditorPane;
import nl.infcomtec.graphs.ClNode;
import nl.infcomtec.tools.PandocConverter;

/**
 * Show a node in full detail with some editing options.
 *
 * @author Walter Stroebel
 */
public class NodePanel extends javax.swing.JPanel {

    private final static AtomicReference<Font> dFont = new AtomicReference<>(PersonAI.font);
    private final ClNode node;
    private final Action repAct;
    private final Action rebuild;
    private final PersonAI owner;

    /**
     * Creates new form NodePanel
     *
     * @param node
     * @param repAct call this to repaint.
     * @param rebuild call this to rebuild.
     */
    public NodePanel(PersonAI owner, ClNode node, Action repAct, Action rebuild) {
        this.owner=owner;
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

        jScrollPane1 = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        tfLabel = new javax.swing.JTextField();
        jSlider1 = new javax.swing.JSlider();
        cbShapes = new javax.swing.JComboBox<>();
        btApply = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setFont(dFont.get());
        jScrollPane1.setViewportView(editorPane);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        tfLabel.setColumns(20);
        tfLabel.setText(node.label);
        tfLabel.setBorder(BorderFactory.createTitledBorder("Display label"));
        jPanel1.add(tfLabel);
        jPanel1.add(Box.createVerticalGlue());

        jSlider1.setMaximum(48);
        jSlider1.setMinimum(6);
        jSlider1.setToolTipText("Change the size of the displayed text.");
        jSlider1.setBorder(BorderFactory.createTitledBorder("Font size"));
        jSlider1.setValue(24);
        
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });
        jPanel1.add(jSlider1);

        cbShapes.setEditable(true);
        cbShapes.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"ellipse", "box", "circle", "diamond", "rectangle", "plaintext", "triangle", "hexagon", "octagon", "parallelogram"}));
        jPanel1.add(cbShapes);

        btApply.setText("Apply");
        btApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btApplyActionPerformed(evt);
            }
        });
        jPanel1.add(btApply);

        add(jPanel1, java.awt.BorderLayout.EAST);
    }

    private void btApplyActionPerformed(java.awt.event.ActionEvent evt) {
        node.label = tfLabel.getText();
        node.setShape(cbShapes.getSelectedItem().toString());
        rebuild.actionPerformed(null);
    }

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {
        int s = jSlider1.getValue();
        if (s != dFont.get().getSize()) {
            dFont.set(new Font(dFont.get().getFontName(), dFont.get().getStyle(), s));
            editorPane.setFont(dFont.get());
            repAct.actionPerformed(null);
        }
    }

    private javax.swing.JButton btApply;
    private javax.swing.JComboBox<String> cbShapes;
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTextField tfLabel;
}
