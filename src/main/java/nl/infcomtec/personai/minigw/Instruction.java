package nl.infcomtec.personai.minigw;

/**
 *
 * @author walter
 */

public class Instruction {
    public enum ArgumentType {
        IMMEDIATE_PARENTS,
        IMMEDIATE_CHILDREN,
        WHOLE_PARENT,
        WHOLE_BRANCH,
        // Add other types as necessary
    }

    private final String description;
    private final String prompt;
    private final ArgumentType arguments;

    public Instruction(final String description, final String prompt, final ArgumentType arguments) {
        this.description = description;
        this.prompt = prompt;
        this.arguments = arguments;
    }

    public String getDescription() {
        return description;
    }

    public String getPrompt() {
        return prompt;
    }

    public ArgumentType getArguments() {
        return arguments;
    }

}
