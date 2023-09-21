package nl.infcomtec.personai.minigw;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Dialogs {

    private final List<DialogStep> mainTopics = new ArrayList<>();
    private final TreeMap<String, Integer> index = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final CustomGUI gui;
    private int currentTopic = 0;
    private Box ourBox;
    private final AtomicReference<ActiveBox> active = new AtomicReference<>();

    public Dialogs(CustomGUI gui) {
        this.gui = gui;
    }

    public synchronized String makeUnique(String topic) {
        int suf = 0;
        String ret = topic;
        while (index.containsKey(ret)) {
            suf++;
            ret = String.format("%s (%d)", topic, suf);
        }
        return ret;
    }

    public synchronized void addMainTopic(DialogStep topic) {
        topic.subject = makeUnique(topic.subject);
        currentTopic = mainTopics.size();
        mainTopics.add(topic);
        reIndex();
    }

    private synchronized void reIndex() {
        index.clear();
        for (int i = 0; i < mainTopics.size(); i++) {
            index.put(mainTopics.get(i).subject, i);
        }
        // Trigger layout update
        for (int i = 0; null != ourBox && i < gui.tabbedPane.getComponentCount(); i++) {
            if (gui.tabbedPane.getComponentAt(i).equals(ourBox)) {
                gui.tabbedPane.setComponentAt(i, ourBox = getTopics());
            }
        }
        gui.frame.repaint();
    }

    public synchronized void moveTopicUp() {
        if (currentTopic > 0) {
            mainTopics.add(currentTopic - 1, mainTopics.remove(currentTopic));
            currentTopic--;
            reIndex();
        }
    }

    public synchronized void moveTopicDown() {
        if (currentTopic < mainTopics.size() - 1) {
            mainTopics.add(currentTopic + 1, mainTopics.remove(currentTopic));
            currentTopic++;
            reIndex();
        }
    }

    public synchronized void moveTopicFirst() {
        if (currentTopic > 0) {
            mainTopics.add(0, mainTopics.remove(currentTopic));
            currentTopic = 0;  // Update to point to the first position
            reIndex();
        }
    }

    public synchronized void moveTopicLast() {
        int lastIndex = mainTopics.size() - 1;
        if (currentTopic < lastIndex) {
            mainTopics.add(mainTopics.remove(currentTopic));
            currentTopic = lastIndex;  // Update to point to the last position
            reIndex();
        }
    }

    public synchronized void setLink(String otherTopic) {
        Integer k = index.get(otherTopic);
        if (null != k) {
            mainTopics.get(currentTopic).link = mainTopics.get(k);
        }
    }

    private Box display(final DialogStep step, boolean current) {
        Box ret = Box.createHorizontalBox();
        ret.setOpaque(true);
        if (current) {
            ret.setBackground(Color.LIGHT_GRAY);
            final ActiveBox vert = new ActiveBox(step, new JTextField(step.subject), new JTextArea(step.text, 24, 80));
            active.set(vert);
            ret.add(vert);
        } else {
            ret.setBackground(Color.MAGENTA);
            ret.add(Box.createHorizontalGlue());
            ret.add(new JLabel(step.subject));
            ret.add(Box.createHorizontalGlue());
            ret.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        ActiveBox a = active.get();
                        if (null != a) {
                            a.update();
                        }
                        currentTopic = index.get(step.subject);
                        reIndex();
                    }
                }
            });
        }
        return ret;
    }

    public synchronized Box getTopics() {
        Box ret = Box.createVerticalBox();
        for (int i = 0; i < mainTopics.size(); i++) {
            Box hor = display(mainTopics.get(i), i == currentTopic);
            ret.add(hor);
        }
        ourBox = ret;
        return ret;
    }

    private class ActiveBox extends Box {

        private final JTextField tfSubject;
        private final JTextArea tfText;
        private final DialogStep step;

        public ActiveBox(final DialogStep step, final JTextField tfSubject, final JTextArea tfText) {
            super(BoxLayout.Y_AXIS);
            this.step = step;
            this.tfSubject = tfSubject;
            this.tfText = tfText;
            add(tfSubject);
            Box buttons = Box.createHorizontalBox();
            buttons.add(new JButton(new AbstractAction(" top ") {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    update();
                    moveTopicFirst();
                }
            }));
            buttons.add(new JButton(new AbstractAction(" up ") {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    update();
                    moveTopicUp();
                }
            }));
            buttons.add(new JButton(new AbstractAction(" down ") {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    update();
                    moveTopicDown();
                }
            }));
            buttons.add(new JButton(new AbstractAction(" last ") {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    update();
                    moveTopicLast();
                }
            }));
            add(buttons);
            add(new JScrollPane(tfText));
        }

        private void update() {
            String nSub = tfSubject.getText();
            if (!nSub.equalsIgnoreCase(step.subject)) {
                step.subject = makeUnique(nSub);
            } else {
                step.subject = nSub; // could have change case
            }
            step.text = tfText.getText();
        }

    }
}
