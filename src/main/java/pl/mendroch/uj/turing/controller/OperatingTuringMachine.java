package pl.mendroch.uj.turing.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Pair;
import lombok.extern.java.Log;
import org.controlsfx.control.Notifications;
import pl.mendroch.uj.turing.model.MachineState;
import pl.mendroch.uj.turing.model.Move;
import pl.mendroch.uj.turing.model.Transition;
import pl.mendroch.uj.turing.model.TuringMachine;
import pl.mendroch.uj.turing.view.Dialog;
import pl.mendroch.uj.turing.view.MainWindowController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static pl.mendroch.uj.turing.model.Move.*;
import static pl.mendroch.uj.turing.model.TuringMachineConstants.*;

@Log
@SuppressWarnings("WeakerAccess")
public class OperatingTuringMachine {
    public static final String MODIFY_ENABLE_DEBUG_OR_RESTART = "Zmodyfikuj przejścia i włącz tryb debugowania lub zrestartuj maszynę";
    private final BooleanProperty debugMode = new SimpleBooleanProperty();
    private final BooleanProperty detectLoop = new SimpleBooleanProperty();
    private final ObservableLinkedList transitionHistory = new ObservableLinkedList();
    private final TuringMachine machine;
    private HBox machineTape;
    private HBox operatingTape;
    private LinkedList<String> tape;
    private IntegerProperty stepTime = new SimpleIntegerProperty(1000);
    private Map<String, MachineState> states;
    private int actualPosition = 1;
    private MachineState actualState;
    private SimpleBooleanProperty manual = new SimpleBooleanProperty(true);
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private ObservableList<Node> machineTapeList;
    private ObservableList<Node> operatingTapeList;
    private TableView<Transition> transitionTable;

    public OperatingTuringMachine(TuringMachine machine) {
        this.machine = machine;
        debugMode.addListener(observable -> debugChanged());
    }

    public void setTapePanels(HBox machineTape, HBox operatingTape, TableView<Transition> transitionTable) {
        this.machineTape = machineTape;
        this.operatingTape = operatingTape;
        this.transitionTable = transitionTable;
    }

    public void initialize() {
        tape = machine.getTape();
        clearHistory();
        states = new HashMap<>(machine.getStates());
        actualPosition = 1;
        actualState = states.get(INITIAL_STATE);
        machineTapeList = machineTape.getChildren();
        operatingTapeList = operatingTape.getChildren();
        machineTapeList.clear();
        operatingTapeList.clear();
        for (String character : tape) {
            if (character.equals(END_CHARACTER)) {
                addTapeCharacterLabel(END_CHARACTER, LIMIT_STYLE_CLASS);
            } else {
                addTapeCharacterLabel(character, TAPE_STYLE_CLASS);
            }
        }
        machineTapeList.get(1).getStyleClass().add(SELECTED_STYLE_CLASS);
        operatingTapeList.get(1).getStyleClass().add(SELECTED_STYLE_CLASS);
        selectTableRow();
    }

    public void selectTableRow() {
        Optional<Transition> transition = actualState.getTransition(tape.get(actualPosition));
        if (transition.isPresent()) {
            transitionTable.getSelectionModel().select(transition.get());
        } else {
            transitionTable.getSelectionModel().clearSelection();
        }
    }

    private void addTapeCharacterLabel(String character, String styleClass) {
        machineTapeList.add(createLabel(character, styleClass));
        operatingTapeList.add(createLabel(character, styleClass));
    }

    private void addTapeCharacterLabelAtStart(String character, String styleClass) {
        machineTapeList.add(0, createLabel(character, styleClass));
        operatingTapeList.add(0, createLabel(character, styleClass));
    }

    private Label createLabel(String endCharacter, String limitStyleClass) {
        Label label = new Label();
        label.setText(endCharacter);
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.getStyleClass().add(limitStyleClass);
        return label;
    }

    public BooleanProperty debugModeProperty() {
        return debugMode;
    }

    public IntegerProperty stepTimeProperty() {
        return stepTime;
    }

    private void debugChanged() {
        if (!debugMode.get()) {
            states = new HashMap<>(machine.getStates());
        }
    }

    public synchronized void stepBack() {
        if (transitionHistory.isEmpty()) {
            return;
        }
        Transition transition = transitionHistory.pollLast().getKey();
        move(transition.getTo(), transition.getThen(), transition.getMove(), true);
    }

    public synchronized void step() {
        String character = tape.get(actualPosition);
        Optional<Transition> transitionForState = actualState.getTransition(character);
        if (transitionForState.isPresent()) {
            Transition transition = transitionForState.get();
            String then = isAny(transition.getThen()) ? isAny(transition.getWhen()) ? character : transition.getWhen() : transition.getThen();
            if (move(transition.getTo(), then, transition.getMove(), false)) {
                transitionHistory.addLast(new Pair<>(new Transition()
                        .from(transition.getTo())
                        .to(transition.getFrom())
                        .move(transition.getMove().revert())
                        .when(then)
                        .then(isAny(transition.getWhen()) ? isAny(transition.getThen()) ? character : transition.getThen() : transition.getWhen()),
                        getTapeString(tape)));
                if (detectLoop.get()) {
                    executor.submit(() -> checkIfWillLoop(transition, tape, actualPosition));
                    executor.submit(() -> checkIfLooped(new LinkedList<>(transitionHistory)));
                }
                return;
            }
        } else {
            Dialog.showWarning(format("Brak przejścia zdefiniowanego dla stanu %s i znaku %s", actualState.getName(), character));
        }
        setManual(true);
    }

    private String getTapeString(LinkedList<String> tape) {
        StringBuilder builder = new StringBuilder();
        tape.forEach(builder::append);
        return builder.toString();
    }

    private void checkIfWillLoop(Transition transition, LinkedList<String> tape, int actualPosition) {
        if (PRAWO.equals(transition.getMove())) {
            for (int i = actualPosition; i < tape.size(); i++) {
                if (!isNotCharacter(tape.get(i))) {
                    return;
                }
            }
            Optional<Transition> nextTransition = machine.getState(transition.getTo()).getTransition(BLANK_CHARACTER);
            nextTransition.ifPresent(next -> {
                if (PRAWO.equals(next.getMove()) && isNotCharacter(next.getWhen()) && isNotCharacter(next.getThen())) {
                    willLoop(transition, next);
                }
            });
        } else if (LEWO.equals(transition.getMove())) {
            for (int i = actualPosition; i >= 0; i--) {
                if (!(BLANK_CHARACTER.equals(tape.get(i)) || END_CHARACTER.equals(tape.get(i)))) {
                    return;
                }
            }
            Optional<Transition> nextTransition = machine.getState(transition.getTo()).getTransition(BLANK_CHARACTER);
            nextTransition.ifPresent(next -> {
                if (LEWO.equals(next.getMove()) && isNotCharacter(next.getWhen()) && isNotCharacter(next.getThen())) {
                    willLoop(transition, next);
                }
            });
        }
    }

    private boolean isNotCharacter(String character) {
        return BLANK_CHARACTER.equals(character) || ANY_CHARACTER.equals(character);
    }

    private void willLoop(Transition transition, Transition next) {
        manual.set(true);
        log.warning("Runner will loop: " + next);
        Platform.runLater(() -> Notifications.create()
                .title("Maszyna Turinga się zapętli")
                .owner(MainWindowController.getStage())
                .text("Zapętli się na przejściu: " + transition + "\n" + MODIFY_ENABLE_DEBUG_OR_RESTART)
                .showError());
    }

    private void checkIfLooped(LinkedList<Pair<Transition, String>> list) {
        log.info("check if looped with history size: " + list.size());
        Pair<Transition, String> transitionPair = list.removeLast();
        Transition transition = transitionPair.getKey();
        if (BEZ.equals(transition.getMove()) && transition.deepEquals(list.pollLast())) {
            log.warning("Runner looped on transition: " + transition);
            Platform.runLater(() -> Notifications.create()
                    .title("Maszyna Turinga zapętliła się")
                    .owner(MainWindowController.getStage())
                    .text("Zapętlona na przejściu: " + transition + "\n" + MODIFY_ENABLE_DEBUG_OR_RESTART)
                    .showError());
            return;
        }
        int position = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            Pair<Transition, String> nextTransitionPair = list.get(i);
            Transition next = nextTransitionPair.getKey();
            position += next.getMove().move(0);
            if (position == 0 && transition.deepEquals(next) && getTapeString(tape).equals(nextTransitionPair.getValue())) {
                log.warning("Runner looped on transition: " + transition);
                int steps = list.size() - i;
                if (!debugMode.get()) {
                    setManual(true);
                }
                Platform.runLater(() -> Notifications.create()
                        .title("Maszyna Turinga zapętliła się")
                        .owner(MainWindowController.getStage())
                        .text("Maszyna Turinga była " + steps + " przejść temu w tej samej pozycji i tym samym przejściu: " + next)
                        .showWarning());
                return;
            }
            //TODO check if looped
        }
    }

    private boolean isAny(String character) {
        return ANY_CHARACTER.equals(character);
    }

    private synchronized boolean move(String to, String then, Move move, boolean stepBack) {
        int nextIndex = move.move(actualPosition);
        if (validNextIndex(nextIndex)) {
            return false;
        }
        nextIndex = move.move(actualPosition);
        if (!stepBack) {
            tape.set(actualPosition, then);
            ((Label) operatingTapeList.get(actualPosition)).setText(then);
        }
        if (actualPosition != nextIndex) {
            changeStyleClass(machineTapeList, actualPosition, SELECTED_STYLE_CLASS, TAPE_STYLE_CLASS);
            changeStyleClass(machineTapeList, nextIndex, TAPE_STYLE_CLASS, SELECTED_STYLE_CLASS);
            changeStyleClass(operatingTapeList, actualPosition, SELECTED_STYLE_CLASS, TAPE_STYLE_CLASS);
            changeStyleClass(operatingTapeList, nextIndex, TAPE_STYLE_CLASS, SELECTED_STYLE_CLASS);
        }
        actualPosition = nextIndex;
        if (stepBack) {
            tape.set(actualPosition, then);
            ((Label) operatingTapeList.get(actualPosition)).setText(then);
        }
        if (debugMode.get()) {
            actualState = machine.getState(to);
        } else {
            actualState = states.get(to);
        }
        selectTableRow();
        return true;
    }

    private void changeStyleClass(ObservableList<Node> list, int position, String remove, String add) {
        ObservableList<String> styleClassList = list.get(position).getStyleClass();
        styleClassList.remove(remove);
        styleClassList.add(add);
    }

    private boolean validNextIndex(int nextIndex) {
        if (nextIndex == 0) {
            if (tape.get(0).equals(END_CHARACTER)) {
                Dialog.showError("Taśma lewostronnie ograniczona. " + MODIFY_ENABLE_DEBUG_OR_RESTART);
                return true;
            }
            tape.add(0, BLANK_CHARACTER);
            addTapeCharacterLabelAtStart(BLANK_CHARACTER, TAPE_STYLE_CLASS);
            actualPosition++;
        } else if (nextIndex == tape.size() - 1) {
            if (tape.get(nextIndex).equals(END_CHARACTER)) {
                Dialog.showError("Taśma prawostronnie ograniczona. " + MODIFY_ENABLE_DEBUG_OR_RESTART);
                return true;
            }
            addTapeCharacterLabel(BLANK_CHARACTER, TAPE_STYLE_CLASS);
            tape.add(BLANK_CHARACTER);
        }
        return false;
    }

    public BooleanProperty emptyHistoryProperty() {
        return transitionHistory.empty;
    }

    public boolean isManual() {
        return manual.get();
    }

    public void setManual(boolean manual) {
        this.manual.set(manual);
    }

    public SimpleBooleanProperty manualProperty() {
        return manual;
    }

    public void finish() {
        executor.shutdownNow();
    }

    public BooleanProperty detectLoopProperty() {
        return detectLoop;
    }

    @Override
    protected void finalize() throws Throwable {
        if (executor != null) {
            executor.shutdownNow();
        }
        super.finalize();
    }

    public int getStepTime() {
        return stepTime.get();
    }

    public void clearHistory() {
        transitionHistory.clear();
    }

    public void setInitialState(String initialState) {
        actualState = machine.getState(initialState);
        selectTableRow();
    }

    public static class ObservableLinkedList extends LinkedList<Pair<Transition, String>> {
        private BooleanProperty empty = new SimpleBooleanProperty(true);

        @Override
        public void addLast(Pair<Transition, String> stateTransition) {
            super.addLast(stateTransition);
            empty.set(false);
        }

        @Override
        public Pair<Transition, String> pollLast() {
            empty.set(this.size() - 1 == 0);
            return super.pollLast();
        }

        @Override
        public void clear() {
            super.clear();
            empty.set(true);
        }
    }
}
