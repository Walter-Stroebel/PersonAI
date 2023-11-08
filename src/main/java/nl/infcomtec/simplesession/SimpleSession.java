package nl.infcomtec.simplesession;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import nl.infcomtec.personai.Instruction;
import nl.infcomtec.personai.Instructions;
import nl.infcomtec.personai.OpenAIAPI;
import nl.infcomtec.personai.PersonAI;
import nl.infcomtec.personai.TraceLogger;
import nl.infcomtec.tools.PandocConverter;

/**
 * A way more simple improved chat session.
 *
 * @author Walter Stroebel
 */
public class SimpleSession {

    public static final String EOLN = System.lineSeparator();

    public static void main(String[] args) throws Exception {
        TraceLogger.traceMe("OpenAI_API.txt");
        if (0 == args.length) {
            Object[] options = {"SimpleSession", "PersonAI"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Which tool would you like to use?",
                    "Tool Selection",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (choice == 1) {
                PersonAI.main(new String[]{});
            } else {
                thisMain(args);
            }
        } else if (args[0].equalsIgnoreCase("PersonAI")) {
            String[] a2 = new String[args.length - 1];
            if (args.length > 1) {
                System.arraycopy(args, 1, a2, 0, a2.length);
            }
            PersonAI.main(a2);
        } else {
            thisMain(args);
        }
    }
    private static final AtomicReference<JFrame> frame = new AtomicReference<>();
    private File defaultDir = new File(System.getProperty("user.home"));

    public static void thisMain(String[] args) throws Exception {
        new SimpleSession();
    }

    public static void rebuild() {
        JFrame frm = frame.get();
        if (null != frm) {
            frm.repaint();
        }
    }
    private final DefaultMutableTreeNode root;
    private final JTree mainTree;
    private final JTextArea userInput;
    private final JEditorPane textPane;
    private JTextArea progMsg;
    private double sessionCost = 0;
    private NodeLLM selectedNode;

    public SimpleSession() {
        PersonAI.doConfig();
        PersonAI.setupGUI();
        frame.set(new JFrame("Simple AI/LLM session"));
        JFrame frm = frame.get();
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setExtendedState(JFrame.MAXIMIZED_BOTH);
        root = new DefaultMutableTreeNode("Questions:");
        mainTree = new JTree(root);
        mainTree.setPreferredSize(new Dimension(PersonAI.config.w20Per, PersonAI.config.hFull));
        // Add a TreeSelectionListener to the JTree
        mainTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath(); // Get the new path that has been selected
                if (path != null) {
                    if (mainTree.isExpanded(path)) {
                        mainTree.collapsePath(path);
                    } else {
                        mainTree.expandPath(path);
                    }
                }
                Object last = mainTree.getLastSelectedPathComponent();
                if (last instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) last;

                    if (dmtn.getUserObject() instanceof NodeLLM) {
                        selectedNode = (NodeLLM) dmtn.getUserObject();
                        String html = new PandocConverter().convertMarkdownToHTML(selectedNode.answer);
                        textPane.setText(html);
                    }
                }
            }
        });

        frm.getContentPane().setLayout(new BorderLayout());
        frm.getContentPane().add(new JScrollPane(mainTree), BorderLayout.WEST);
        loadActions();
        JPanel userInputPanel = new JPanel(new BorderLayout());
        userInput = new JTextArea(10, 132);
        userInput.setLineWrap(true);
        userInput.setWrapStyleWord(true);
        userInput.setPreferredSize(new Dimension(PersonAI.config.w20Per / 5 * 4, PersonAI.config.h20Per));
        userInputPanel.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.green, 3),
                        "Your questions"));
        userInputPanel.add(new JScrollPane(userInput), BorderLayout.CENTER);
        Box buttons = getUserButtons();
        userInputPanel.add(buttons, BorderLayout.EAST);
        frm.getContentPane().add(userInputPanel, BorderLayout.SOUTH);
        textPane = new JEditorPane();
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        textPane.setEditable(false);
        textPane.setContentType("text/html");
        String md = PersonAI.getResource("simple.md");
        String html = new PandocConverter().convertMarkdownToHTML(md);
        textPane.setText(html);
        frm.getContentPane().add(new JScrollPane(textPane), BorderLayout.CENTER);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frm = frame.get();
                if (null != frm) {
                    frm.setVisible(true);
                }
            }
        });
    }

    private Box getUserButtons() {
        Box buttons = Box.createVerticalBox();
        buttons.add(Box.createVerticalGlue());
        buttons.add(new JButton(new AbstractAction("Clear input") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                userInput.setText("");
            }
        }));
        buttons.add(new JButton(new AbstractAction("Export...") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JFileChooser jfc = new JFileChooser();
                jfc.setCurrentDirectory(defaultDir);
                int ans = jfc.showSaveDialog(frame.get());
                if (ans == JFileChooser.APPROVE_OPTION) {
                    defaultDir = jfc.getCurrentDirectory();
                    File f = jfc.getSelectedFile();
                    StringBuilder sb = new StringBuilder();
                    Enumeration<TreeNode> en = root.children();
                    while (en.hasMoreElements()) {
                        TreeNode e = en.nextElement();
                        if (e instanceof DefaultMutableTreeNode) {
                            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) e;
                            if (dmtn.getUserObject() instanceof NodeLLM) {
                                NodeLLM n = (NodeLLM) dmtn.getUserObject();
                                sb.append("# Question: ").append(n.question).append(EOLN).append(EOLN);
                                if (!n.text.isEmpty()) {
                                    sb.append("# Original text").append(EOLN).append(n.text).append(EOLN).append(EOLN);
                                }
                                sb.append("# Answer:").append(EOLN).append(EOLN).append(n.answer).append(EOLN).append(EOLN);
                            }
                        }
                    }
                    new PandocConverter().convertMarkdownToFile(sb.toString(), f);
                }
            }
        }));
        buttons.add(new JButton(new AbstractAction("Save...") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JFileChooser jfc = new JFileChooser();
                jfc.setCurrentDirectory(defaultDir);
                int ans = jfc.showSaveDialog(frame.get());
                if (ans == JFileChooser.APPROVE_OPTION) {
                    defaultDir = jfc.getCurrentDirectory();
                    File f = jfc.getSelectedFile();
                    if (!f.getName().endsWith(".json")) {
                        f = new File(f.getAbsolutePath() + ".json");
                    }
                    List<NodeLLM> l = new LinkedList<>();
                    Enumeration<TreeNode> en = root.children();
                    while (en.hasMoreElements()) {
                        TreeNode e = en.nextElement();
                        if (e instanceof DefaultMutableTreeNode) {
                            DefaultMutableTreeNode d = (DefaultMutableTreeNode) e;
                            if (d.getUserObject() instanceof NodeLLM) {
                                l.add((NodeLLM) d.getUserObject());
                            }
                        }
                    }
                    try (FileWriter fw = new FileWriter(f)) {
                        PersonAI.gson.toJson(l, fw);
                        fw.write(EOLN);
                    } catch (IOException ex) {
                        Logger.getLogger(SimpleSession.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }));
        buttons.add(new JButton(new AbstractAction("Load...") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileFilter(new FileNameExtensionFilter("AI Session FILES", "json"));
                jfc.setCurrentDirectory(defaultDir);
                int ans = jfc.showOpenDialog(frame.get());
                if (ans == JFileChooser.APPROVE_OPTION) {
                    defaultDir = jfc.getCurrentDirectory();
                    File f = jfc.getSelectedFile();
                    try (FileReader fr = new FileReader(f)) {
                        NodeLLM[] fromJson = PersonAI.gson.fromJson(fr, NodeLLM[].class);
                        root.removeAllChildren();
                        for (NodeLLM aj : fromJson) {
                            DefaultMutableTreeNode aNode = new DefaultMutableTreeNode(aj);
                            root.add(aNode);
                            extentNodeLLM(aNode, aj);
                        }
                        ((DefaultTreeModel) mainTree.getModel()).reload();
                    } catch (IOException ex) {
                        Logger.getLogger(SimpleSession.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }));
        buttons.add(Box.createVerticalGlue());
        buttons.add(new JButton(new SubmitAction()));
        return buttons;
    }

    private void loadActions() {
        Instructions ins = Instructions.load(new File(PersonAI.WORK_DIR, PersonAI.INS_FILENAME), PersonAI.gson);
        JToolBar tb = new JToolBar(JToolBar.VERTICAL);
        tb.setFloatable(false);
        for (final Instruction i : ins.insList) {
            JButton jb = new JButton(new AbstractAction(i.description) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    final NodeLLM answer = new NodeLLM();
                    answer.question = i.prompt;
                    answer.text = new PandocConverter().convertHTMLToMarkdown(textPane.getText());
                    callAPI(answer);
                }
            });
            jb.setToolTipText(i.prompt);
            tb.add(jb);
        }
        JButton jb = new JButton(new AbstractAction("Ask your own") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                final NodeLLM answer = new NodeLLM();
                answer.question = userInput.getText();
                answer.text = new PandocConverter().convertHTMLToMarkdown(textPane.getText());
                callAPI(answer);
            }
        });
        jb.setToolTipText("Ask a follow-up question on the text to the left.");
        jb.setForeground(Color.MAGENTA);
        tb.add(jb);
        progMsg = new JTextArea();
        progMsg.setFont(new Font(Font.MONOSPACED, Font.PLAIN, PersonAI.config.fontSize));
        tb.add(new JScrollPane(progMsg));
        frame.get().getContentPane().add(tb, BorderLayout.EAST);
    }

    private void callAPI(final NodeLLM answer) {
        String system = "You are a helpful assistant. Always provide a relevant and proactive response.";
        progMsg.setText("Calling LLM..." + EOLN);
        progMsg.setForeground(Color.BLUE.brighter());
        final SimpleRequestWorker worker = new SimpleRequestWorker(progMsg, system, answer.question, answer.text);
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    worker.completed.acquire();
                    final DefaultMutableTreeNode aNode = new DefaultMutableTreeNode(answer);
                    answer.answer = worker.answer;
                    answer.at = System.currentTimeMillis();
                    answer.inputTokens = worker.promptTokens.get();
                    answer.outputTokens = worker.outputTokens.get();
                    answer.cost = worker.cost;
                    extentNodeLLM(aNode, answer);
                    sessionCost += answer.cost;
                    progMsg.append(String.format("\nSession cost:\n%.2f", sessionCost));
                    final String ans = new PandocConverter().convertMarkdownToHTML(answer.answer);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            root.add(aNode);
                            ((DefaultTreeModel) mainTree.getModel()).reload();
                            textPane.setText(ans);
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(SimpleSession.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        new Thread(runnable).start();
        worker.execute();
    }

    private void extentNodeLLM(final DefaultMutableTreeNode aNode, NodeLLM answer) {
        {
            DefaultMutableTreeNode t = new DefaultMutableTreeNode(
                    String.format("At: %1$tF %1$tT", answer.at));
            aNode.add(t);
        }
        {
            DefaultMutableTreeNode t = new DefaultMutableTreeNode(
                    String.format("Input: %d tokens, %f cents", answer.inputTokens, answer.inputTokens * OpenAIAPI.ITC));
            aNode.add(t);
        }
        {
            DefaultMutableTreeNode t = new DefaultMutableTreeNode(
                    String.format("Output: %d tokens, %f cents", answer.outputTokens, answer.outputTokens * OpenAIAPI.OTC));
            aNode.add(t);
        }
        {
            DefaultMutableTreeNode t = new DefaultMutableTreeNode(
                    String.format("Total cost: %f cents", answer.cost));
            aNode.add(t);
        }
    }

    private class SubmitAction extends AbstractAction {

        public SubmitAction() {
            super("Send to LLM");
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            final NodeLLM answer = new NodeLLM();
            answer.question = userInput.getText();
            callAPI(answer);
        }

    }

    public static class NodeLLM {

        public String question = "";
        public String answer = "";
        public String text = "";
        private long at;
        private int inputTokens;
        private int outputTokens;
        private double cost;

        public NodeLLM() {
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            String q = (question.length() > 23) ? "..." + question.substring(question.length() - 20) : question;
            String a = (answer.length() > 23) ? "..." + answer.substring(answer.length() - 20) : answer;
            sb.append("Q:").append(q);
            sb.append(" A:").append(a);
            return sb.toString();
        }
    }
}
