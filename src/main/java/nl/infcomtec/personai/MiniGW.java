package nl.infcomtec.personai;

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
    public static final File WORK_DIR = new File(HOME_DIR,".personAI");
    public static final File VAGRANT_DIR = new File(HOME_DIR, "vagrant/MiniGW");
    public static final File VAGRANT_KEY = new File(VAGRANT_DIR, ".vagrant/machines/default/virtualbox/private_key");
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        WORK_DIR.mkdirs(); // in case it doesn't
        if(!WORK_DIR.exists()){
            System.err.println("Cannot access nor create work directory?");
            System.exit(1);
        }
        CustomGUI gui = new CustomGUI();
        gui.setVisible();
    }

}
