package nl.infcomtec.llmtools.minigw;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.swing.Box;

public class Dialogs {

    private final List<DialogStep> mainTopics = new ArrayList<>();
    private final TreeMap<String, Integer> index = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final CustomGUI gui;
    private int currentTopic = 0;

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
        gui.frame.repaint();
    }

    public synchronized void moveTopicUp() {
        if (currentTopic > 0) {
            mainTopics.add(currentTopic - 1, mainTopics.remove(currentTopic));
            reIndex();
            currentTopic--;
        }
    }

    public synchronized void moveTopicDown() {
        if (currentTopic < mainTopics.size() - 1) {
            mainTopics.add(currentTopic + 1, mainTopics.remove(currentTopic));
            reIndex();
            currentTopic++;
        }
    }

    public synchronized void moveTopicFirst() {
        if (currentTopic > 0) {
            mainTopics.add(0, mainTopics.remove(currentTopic));
            reIndex();
            currentTopic = 0;  // Update to point to the first position
        }
    }

    public synchronized void moveTopicLast() {
        int lastIndex = mainTopics.size() - 1;
        if (currentTopic < lastIndex) {
            mainTopics.add(mainTopics.remove(currentTopic));
            reIndex();
            currentTopic = lastIndex;  // Update to point to the last position
        }
    }

    public synchronized void setLink(String otherTopic) {
        Integer k = index.get(otherTopic);
        if (null != k) {
            mainTopics.get(currentTopic).link = mainTopics.get(k);
        }
    }

    public synchronized Box getTopics() {
        Box ret = Box.createVerticalBox();
        for (int i = 0; i < mainTopics.size(); i++) {
            Box hor = mainTopics.get(i).display(i == currentTopic);
            ret.add(hor);
        }
        return ret;
    }
}
