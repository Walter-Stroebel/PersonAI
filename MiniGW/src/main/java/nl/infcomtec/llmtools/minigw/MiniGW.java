package nl.infcomtec.llmtools.minigw;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;

/**
 * Main class and global members.
 *
 * @author Walter Stroebel
 */
public class MiniGW {

    public static final File HOME_DIR = new File(System.getProperty("user.home"));
    public static final File VAGRANT_DIR = new File(HOME_DIR, "vagrant/MiniGW");
    public static final File VAGRANT_KEY = new File(VAGRANT_DIR, ".vagrant/machines/default/virtualbox/private_key");
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        new Server().start();
    }

}
