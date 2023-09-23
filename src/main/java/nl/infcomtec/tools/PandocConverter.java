package nl.infcomtec.tools;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class PandocConverter extends ToolManager {

    private String output;

    public String convertMarkdownToHTML(String markdownInput) {
        setInput(markdownInput);
        // Set up the command to run Pandoc
        setCommand("pandoc", "-f", "markdown", "-t", "html");
        run();
        return output;
    }

    public String convertMarkdownToText132(String markdownInput) {
        setInput(markdownInput);
        // Set up the command to run Pandoc --wrap=auto  -f markdown -t plain
        setCommand("pandoc", "--wrap=auto", "--columns=132","-f", "markdown", "-t", "plain");
        run();
        return output;
    }

    @Override
    public void run() {

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
