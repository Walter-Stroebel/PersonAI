/*
 */
package nl.infcomtec.personai;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * List of Instruction objects.
 *
 * @author Walter Stroebel.
 */
public class Instructions {

    public List<Instruction> insList;

    public static Instructions load(File file, Gson gson) {
        if (file.exists()) {
            try (FileReader fr = new FileReader(file)) {
                return gson.fromJson(fr, Instructions.class);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            try (InputStreamReader isr = new InputStreamReader(Instructions.class.getResourceAsStream("/" + PersonAI.INS_FILENAME))) {
                Instructions ret = gson.fromJson(isr, Instructions.class);
                ret.save(file, gson);
                return ret;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void save(File file, Gson gson) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            gson.toJson(this, fw);
            fw.write(System.lineSeparator());
        }
    }
}
