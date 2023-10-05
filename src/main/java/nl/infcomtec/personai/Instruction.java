package nl.infcomtec.personai;

/**
 * Simple prompt or instruction.
 *
 * @author Walter Stroebel.
 */
public class Instruction {

    public final String description;
    public final String prompt;

    public Instruction(final String description, final String prompt) {
        this.description = description;
        this.prompt = prompt;
    }

    @Override
    public String toString() {
        return description;
    }

}
