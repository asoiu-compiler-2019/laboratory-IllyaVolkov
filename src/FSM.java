import java.util.Set;
import java.util.function.BiFunction;

public class FSM {
    private final int NoNextState = 0;

    private Set<Integer> states;
    private Set<Integer> acceptingStates;
    private Integer initialState;
    private BiFunction<Integer, Character, Integer> nextState;

    public FSM(Set<Integer> states, Set<Integer> acceptingStates, Integer initialState, BiFunction<Integer, Character, Integer> nextState) {
        this.states = states;
        this.acceptingStates = acceptingStates;
        this.initialState = initialState;
        this.nextState = nextState;
    }

    public String run(String input) {
        String lastAcceptedInput = null;
        int currentState = this.initialState;

        for (int i = 1, length = input.length(); i <= length; ++i) {
            char character = input.charAt(i);
            int nextState = this.nextState.apply(currentState, character);
            if (this.acceptingStates.contains(nextState)) {
                lastAcceptedInput = input.substring(0, i + 1);
            }

            if (nextState == NoNextState) {
                break;
            }

            currentState = nextState;
        }

        return lastAcceptedInput;
    }
}
