package nl.infcomtec.personai;

import java.awt.Font;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.swing.JEditorPane;
import nl.infcomtec.graphs.ClNode;
import nl.infcomtec.tools.PandocConverter;

/**
 * Show a node in full detail with some editing options.
 * @author Walter Stroebel
 */
public class NodePanel extends javax.swing.JPanel {

    private final static AtomicReference<Font> dFont = new AtomicReference<>(PersonAI.font);
    private final ClNode node;
    private final Action repAct;
    private final Action rebuild;

    /**
     * Creates new form NodePanel
     * @param node
     * @param repAct call this to repaint.
     * @param rebuild call this to rebuild.
     */
    public NodePanel(ClNode node,Action repAct,Action rebuild) {
        this.node = node;
        this.repAct=repAct;
        this.rebuild=rebuild;
        initComponents();
        jEditorPane1.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        jEditorPane1.setText(new PandocConverter().convertMarkdownToHTML(node.getUserStr()));
        tfLabel.setText(node.label);
        jComboBox1.setSelectedItem(node.getShape());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        tfLabel = new javax.swing.JTextField();
        jSlider1 = new javax.swing.JSlider();
        jComboBox1 = new javax.swing.JComboBox<>();
        btApply = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jEditorPane1.setEditable(false);
        jEditorPane1.setContentType("text/html");
        jEditorPane1.setFont(dFont.get());
        jScrollPane1.setViewportView(jEditorPane1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        tfLabel.setColumns(20);
        tfLabel.setText("jTextField1");
        jPanel1.add(tfLabel);

        jSlider1.setMaximum(48);
        jSlider1.setMinimum(6);
        jSlider1.setToolTipText("Font size");
        jSlider1.setValue(24);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });
        jPanel1.add(jSlider1);

        jComboBox1.setEditable(true);
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ellipse", "box", "circle", "diamond", "rectangle", "plaintext", "triangle", "hexagon", "octagon", "parallelogram" }));
        jPanel1.add(jComboBox1);

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
        node.setShape(jComboBox1.getSelectedItem().toString());
        rebuild.actionPerformed(null);
    }

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {
        int s = jSlider1.getValue();
        if (s != dFont.get().getSize()) {
            dFont.set(new Font(dFont.get().getFontName(), dFont.get().getStyle(), s));
            jEditorPane1.setFont(dFont.get());
            repAct.actionPerformed(null);
        }
    }


    private javax.swing.JButton btApply;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTextField tfLabel;
}
