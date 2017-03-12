package pl.mendroch.uj.turing.model;

import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static pl.mendroch.uj.turing.model.TuringMachineConstants.ANY_CHARACTER;

@Getter
@ToString
@SuppressWarnings({"WeakerAccess", "unused"})
public class MachineState {
    private final String name;
    private final Map<String, Transition> transitionMap = new HashMap<>();

    public MachineState(String name) {
        this.name = name;
    }

    public Optional<Transition> getTransition(String character) {
        Transition transition = transitionMap.get(character);
        if (transition != null){
            return Optional.of(transition);
        }
        return Optional.ofNullable(transitionMap.get(ANY_CHARACTER));
    }

    public Transition removeTransition(String character) {
        return transitionMap.remove(character);
    }

    public void putTransition(Transition transition) {
        transitionMap.put(transition.getWhen(), transition);
    }
}
