package simulator;

/**
 * Used to represent condition registers such as the N/Z/P registers
 */
public class ConditionRegister extends Register {

    /**
     * Creates a new condition code register
     *
     * @param name
     *            Name of the register
     */
    public ConditionRegister(String name) {
        super(name);
    }
}
