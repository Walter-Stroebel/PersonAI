package nl.infcomtec.tools;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class PandocConverter extends ToolManager {

    private String output;

    public String convertMarkdownToHTML(String markdownInput) {

        // Return the converted HTML as a String
        return stdoutStream.toString();
    }

    @Override
    public void run() {
        // Set up the command to run Pandoc
        setCommand("pandoc", "-f", "markdown", "-t", "html");

        // Run the command
        internalRun();

        // If the exit code isn't 0, throw an exception
        if (exitCode != 0) {
            output = "Running pandoc failed, rc=" + exitCode;
        } else {
            ByteArrayOutputStream str;
            if (stdoutStream instanceof ByteArrayOutputStream) {
                output = new String(((ByteArrayOutputStream) stdoutStream).toByteArray(), StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * @return the output
     */
    public String getOutput() {
        return output;
    }
}
