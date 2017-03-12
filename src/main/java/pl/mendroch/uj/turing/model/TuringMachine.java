package pl.mendroch.uj.turing.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static pl.mendroch.uj.turing.model.Move.BEZ;
import static pl.mendroch.uj.turing.model.TuringMachineConstants.*;

@SuppressWarnings("unused")
@ToString
public class TuringMachine {
    private final Lock lock = new ReentrantLock();
    private boolean isSingleCharacter = true;
    private StringProperty tape = new SimpleStringProperty();
    private ObservableMap<String, MachineState> states = FXCollections.observableMap(new HashMap<>());
    private ObservableList<Transition> transitions = FXCollections.observableArrayList();
    private BooleanProperty leftLimit = new SimpleBooleanProperty(false);
    private BooleanProperty rightLimit = new SimpleBooleanProperty(false);

    public TuringMachine() {
        addTransition(new Transition()
                .from(INITIAL_STATE)
                .to(INITIAL_STATE)
                .when(ANY_CHARACTER)
                .then(ANY_CHARACTER)
                .move(BEZ)
        );
    }

    public StringProperty tapeProperty() {
        return tape;
    }

    public LinkedList<String> getTape() {
        return createListFromString();
    }

    public void setTape(String tape) {
        this.tape.setValue(tape);
    }

    private LinkedList<String> createListFromString() {
        LinkedList<String> list = new LinkedList<>();
        list.add(leftLimit.get() ? END_CHARACTER : BLANK_CHARACTER);
        String string = tape.get();
        if (isSingleCharacter) {
            string.chars().forEachOrdered(value -> list.add((char) value + ""));
        } else {
            int start = 0;
            for (int i = 1; i < string.length(); i++) {
                char character = string.charAt(i);
                if (character >= 65 && character <= 90 || character == 32) {
                    list.add(string.substring(start, i));
                    start = i;
                }
            }
            if (start < string.length()) {
                list.add(string.substring(start, string.length()));
            }
        }
        list.add(rightLimit.get() ? END_CHARACTER : BLANK_CHARACTER);
        return list;
    }

    public boolean isSingleCharacter() {
        return isSingleCharacter;
    }

    public void setSingleCharacter(boolean singleCharacter) {
        isSingleCharacter = singleCharacter;
    }

    public boolean isLeftLimited() {
        return leftLimit.get();
    }

    public boolean isRightLimited() {
        return rightLimit.get();
    }

    public void addTransition(Transition transition) {
        lock.lock();
        try {
            String from = transition.getFrom();
            MachineState fromState = addState(from);
            String to = transition.getTo();
            MachineState toState = addState(to);
            fromState.putTransition(transition);
            transitions.removeIf(next -> next.equals(transition));
            transitions.add(transition);
        } finally {
            lock.unlock();
        }
    }

    public MachineState addState(String from) {
        return states.computeIfAbsent(from, s -> new MachineState(from));
    }

    public MachineState getState(String state) {
        return states.get(state);
    }

    public void removeState(String state) {
        lock.lock();
        try {
            MachineState remove = states.get(state);
            for (Transition transition : remove.getTransitionMap().values()) {
                transitions.remove(transition);
            }
            for (Transition transition : transitions) {
                if (transition.getTo().equals(state)) {
                    return;
                }
            }
            states.remove(state);
        } finally {
            lock.unlock();
        }
    }

    public void removeTransition(String state, String character) {
        lock.lock();
        try {
            Transition filter = new Transition();
            filter.from(state);
            filter.when(character);
            transitions.removeIf(next -> next.equals(filter));
            MachineState machineState = states.get(state);
            machineState.removeTransition(character);
            if (machineState.getTransitionMap().isEmpty()) {
                for (Transition transition : transitions) {
                    if (transition.getTo().equals(state)) {
                        return;
                    }
                }
                states.remove(state);
            }
        } finally {
            lock.unlock();
        }
    }

    public BooleanProperty leftLimitProperty() {
        return leftLimit;
    }

    public BooleanProperty rightLimitProperty() {
        return rightLimit;
    }

    public ObservableList<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(ObservableList<Transition> transitions) {
        this.transitions = transitions;
    }

    public void reset() {
        isSingleCharacter = true;
        states.clear();
        transitions.clear();
        addTransition(new Transition()
                .from(INITIAL_STATE)
                .to(INITIAL_STATE)
                .when(ANY_CHARACTER)
                .then(ANY_CHARACTER)
                .move(BEZ)
        );
    }

    public ObservableMap<String, MachineState> getStates() {
        return states;
    }

    public String getTapeString() {
        StringBuilder builder = new StringBuilder();
        if (isLeftLimited()) {
            builder.append(END_CHARACTER);
        }
        builder.append(tape.get());
        if (isRightLimited()) {
            builder.append(END_CHARACTER);
        }
        return builder.toString();
    }
}
