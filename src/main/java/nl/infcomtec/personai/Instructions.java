/*
 */
package nl.infcomtec.personai;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JList;

/**
 *
 * @author walter
 */
public class Instructions {

    public List<Instruction> insList;

    public static Instructions load(File file, Gson gson) throws IOException {
        if (file.exists()) {
            try ( FileReader fr = new FileReader(file)) {
                return gson.fromJson(fr, Instructions.class);
            }
        }
        Instructions ret = new Instructions();
        Instruction ins = new Instruction("Explain more",
                "The user is unclear or unsure about the provided answer, please clarify.");
        ret.insList = new ArrayList<>();
        ret.insList.add(ins);
        ret.save(file, gson);
        return ret;
    }

    public JList<Instruction> getAsList() {
        Instruction[] ia = new Instruction[insList.size()];
        insList.toArray(ia);
        return new JList<>(ia);
    }

    public void save(File file, Gson gson) throws IOException {
        try ( FileWriter fw = new FileWriter(file)) {
            gson.toJson(this, fw);
            fw.write(System.lineSeparator());
        }
    }
}
