package pl.mendroch.uj.turing.view;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import lombok.extern.java.Log;
import org.controlsfx.control.table.TableFilter;
import pl.mendroch.uj.turing.controller.OperatingTuringMachine;
import pl.mendroch.uj.turing.controller.TuringMachineRunner;
import pl.mendroch.uj.turing.model.MachineState;
import pl.mendroch.uj.turing.model.Move;
import pl.mendroch.uj.turing.model.Transition;
import pl.mendroch.uj.turing.model.TuringMachine;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Properties;
import java.util.ResourceBundle;

import static pl.mendroch.uj.turing.model.FontStyle.*;
import static pl.mendroch.uj.turing.model.Move.PRAWO;
import static pl.mendroch.uj.turing.model.TuringMachineConstants.END_CHARACTER;
import static pl.mendroch.uj.turing.model.TuringMachineConstants.INITIAL_STATE;
import static pl.mendroch.uj.turing.utilities.StringUtilities.getListFromString;

@Log
public class MainWindowController implements Initializable {
    private static Stage stage;
    private final OperatingTuringMachine operatingMachine;
    private final TuringMachine machine;
    private final TapeListener tapeListener = new TapeListener();
    private TuringMachineRunner runner;

    @FXML
    private CheckMenuItem detectLoop;
    @FXML
    private RadioMenuItem singleMode;
    @FXML
    private RadioMenuItem wordMode;
    @FXML
    private RadioMenuItem small;
    @FXML
    private RadioMenuItem medium;
    @FXML
    private RadioMenuItem big;
    @FXML
    private RadioMenuItem mega;
    @FXML
    private AnchorPane root;
    @FXML
    private TextField tapeInput;
    @FXML
    private CheckBox leftLimit;
    @FXML
    private CheckBox rightLimit;
    @FXML
    private TextField fromDefault;
    @FXML
    private TextField toDefault;
    @FXML
    private TextField whenDefault;
    @FXML
    private TextField thenDefault;
    @FXML
    private ChoiceBox<Move> moveDefault;
    @FXML
    private TextField fromInput;
    @FXML
    private TextField toInput;
    @FXML
    private TextField whenInput;
    @FXML
    private TextField thenInput;
    @FXML
    private ChoiceBox<Move> moveInput;
    @FXML
    private CheckBox clearTransitionFields;
    @FXML
    private CheckBox enableClearAll;
    @FXML
    private Button addTransition;
    @FXML
    private Button clearMachineButton;
    @FXML
    private TableView<Transition> transitionTable;
    @FXML
    private TableColumn<Transition, String> fromColumn;
    @FXML
    private TableColumn<Transition, String> toColumn;
    @FXML
    private TableColumn<Transition, Move> moveColumn;
    @FXML
    private TableColumn<Transition, String> whenColumn;
    @FXML
    private TableColumn<Transition, String> thenColumn;
    @FXML
    private Spinner<Integer> stepBackCount;
    @FXML
    private TextField initialState;
    @FXML
    private Button stepBack;
    @FXML
    private Button stepBackMore;
    @FXML
    private Button step;
    @FXML
    private Button stepMore;
    @FXML
    private Button playPause;
    @FXML
    private Button stop;
    @FXML
    private Spinner<Integer> stepCount;
    @FXML
    private Spinner<Integer> delay;
    @FXML
    private CheckBox debug;
    @FXML
    private HBox machineTape;
    @FXML
    private HBox operatingTape;
    @FXML
    private ScrollPane machineTapeScrollPane;
    @FXML
    private ScrollPane operatingTapeScrollPane;

    public MainWindowController() {
        this.machine = new TuringMachine();
        this.operatingMachine = new OperatingTuringMachine(machine);
    }

    public static Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        MainWindowController.stage = stage;
        loadProperties();
        File file = new File("last.txt");
        if (Files.exists(file.toPath())) {
            log.info("Starting loading last used machine");
            FileDialog.loadMachineFromFile(this, file);
        }
    }

    @FXML
    private void about() {
        Dialog.showAbout();
    }

    @FXML
    private void addTransition() {
        String from = fromInput.getText().trim();
        if (from.isEmpty()) {
            from = fromDefault.getText().trim();
        }
        String to = toInput.getText().trim();
        if (to.isEmpty()) {
            to = toDefault.getText().trim();
            if (to.isEmpty()) {
                to = from;
            }
        }
        Move move = moveInput.getValue();
        if (move == null) {
            move = moveDefault.getValue();
        }
        String when = whenInput.getText();
        if (when.isEmpty()) {
            when = whenDefault.getText();
        }
        String then = thenInput.getText();
        if (then.isEmpty()) {
            then = thenDefault.getText();
        }
        LinkedList<String> whenList = getListFromString(when, machine.isSingleCharacter());
        LinkedList<String> thenList = getListFromString(then, machine.isSingleCharacter());
        if (when.contains(END_CHARACTER) || then.contains(END_CHARACTER)) {
            Dialog.showWarning("Nie może zawierać znaku " + END_CHARACTER);
            return;
        }
        if (thenList.size() > 1) {
            Dialog.showWarning("Nie może zawierać znaku ");
            return;
        }
        for (String whenCharacter : whenList) {
            machine.addTransition(new Transition()
                    .from(from)
                    .to(to)
                    .move(move)
                    .when(whenCharacter)
                    .then(then));
        }
        clearInput();
        transitionTable.refresh();
    }

    private void clearInput() {
        if (clearTransitionFields.isSelected()) {
            fromInput.clear();
            toInput.clear();
            whenInput.clear();
            thenInput.clear();
        }
    }

    @FXML
    public void clearMachine() {
        if (runner != null) {
            debug.setSelected(false);
        }
        clearTapeInput();
        clearTransition();
        initialState.setText(INITIAL_STATE);
        machine.reset();
        singleMode.setSelected(true);
        leftLimit.setSelected(false);
        rightLimit.setSelected(false);
        enableClearAll.setSelected(false);
    }

    @FXML
    private void clearTapeInput() {
        tapeInput.clear();
    }

    @FXML
    private void singleMode() {
        machine.setSingleCharacter(true);
        tapeListener.invalidate();
    }

    @FXML
    private void wordMode() {
        machine.setSingleCharacter(false);
        tapeListener.invalidate();
    }

    @FXML
    private void clearTransition() {
        fromInput.clear();
        toInput.clear();
        whenInput.clear();
        thenInput.clear();
    }

    @FXML
    public void close() {
        File file = new File("last.txt");
        if (runner != null) {
            runner.stop();
        }
        operatingMachine.finish();
        saveProperties();
        FileDialog.saveMachineToFile(stage, machine, file);
        Platform.exit();
    }

    private void saveProperties() {
        log.info("saving properties");
        try (OutputStream out = new FileOutputStream(new File("app.properties"))) {
            Properties props = new Properties();
            File openedFile = FileDialog.getOpenedFile();
            if (openedFile != null) {
                props.put("open.file", openedFile.toString());
            }
            File savedFile = FileDialog.getSavedFile();
            if (savedFile != null) {
                props.put("saved.file", savedFile.toString());
            }
            props.put("debug", String.valueOf(debug.isSelected()));
            props.put("detect.loop", String.valueOf(detectLoop.isSelected()));
            props.put("delay", "" + delay.getValue());
            props.put("step.count", "" + stepCount.getValue());
            props.put("step.back.count", "" + stepBackCount.getValue());
            props.put("initial.state", initialState.getText());
            props.put("font.style", FONT);
            props.store(out, "Turing application properties");
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
        log.info("properties saved");
    }

    @FXML
    private void open() {
        FileDialog.openFile(this);
    }

    @FXML
    private void playPause() {
        if (operatingMachine.isManual()) {
            operatingMachine.setManual(false);
            if (runner == null) {
                runThread();
            } else {
                runner.stepRunner();
            }
        } else {
            operatingMachine.setManual(true);
        }
    }

    private void runThread() {
        operatingMachine.setInitialState(initialState.getText());
        runner = new TuringMachineRunner(operatingMachine);
        new Thread(runner).start();
        stop.setDisable(false);
    }

    @FXML
    private void save() {
        FileDialog.saveToFile(this);
    }

    @FXML
    private void step() {
        setManualAndRunIfNeeded();
        runner.step();
    }

    private void setManualAndRunIfNeeded() {
        operatingMachine.setManual(true);
        if (runner == null) {
            runThread();
        }
    }

    @FXML
    private void stepBack() {
        setManualAndRunIfNeeded();
        runner.stepBack();
    }

    @FXML
    private void stepBackMore() {
        setManualAndRunIfNeeded();
        Integer count = stepBackCount.getValue();
        while (count-- > 0) {
            stepBack();
            try {
                Thread.sleep(delay.getValue() / 100);
            } catch (InterruptedException e) {
                log.warning(e.getMessage());
            }
        }
    }

    @FXML
    private void stepMore() {
        setManualAndRunIfNeeded();
        Integer count = stepCount.getValue();
        while (count-- > 0) {
            step();
            try {
                Thread.sleep(delay.getValue() / 100);
            } catch (InterruptedException e) {
                log.warning(e.getMessage());
            }
        }
    }

    @FXML
    private void stopRunning() {
        runner.stop();
        runner = null;
        initializeRunningElements();
        disableButtons(false);
        tapeListener.invalidate();
    }

    private void initializeRunningElements() {
        operatingMachine.setManual(true);
        stop.setDisable(true);
        operatingMachine.clearHistory();
        machineTape.getChildren().clear();
        operatingTape.getChildren().clear();
    }

    @FXML
    private void fontBig() {
        FONT = BIG;
        root.setStyle(BIG);
    }

    @FXML
    private void fontMedium() {
        FONT = MEDIUM;
        root.setStyle(MEDIUM);
    }

    @FXML
    private void fontMega() {
        FONT = MEGA;
        root.setStyle(MEGA);
    }

    @FXML
    private void fontSmall() {
        FONT = SMALL;
        root.setStyle(SMALL);
    }

    private void disableButtons(boolean disable) {
        step.setDisable(disable);
        stepMore.setDisable(disable);
    }

    TuringMachine getMachine() {
        return machine;
    }

    private void loadProperties() {
        log.info("loading properties");
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(new File("app.properties"))) {
            props.load(is);
            FONT = props.getProperty("font.style");
            switch (FONT) {
                case SMALL:
                    small.setSelected(true);
                    break;
                case MEDIUM:
                    medium.setSelected(true);
                    break;
                case BIG:
                    big.setSelected(true);
                    break;
                case MEGA:
                    mega.setSelected(true);
                    break;
            }
            root.setStyle(FONT);
            FileDialog.setOpenedFile(props.getProperty("open.file"));
            FileDialog.setSavedFile(props.getProperty("saved.file"));
            debug.setSelected(Boolean.valueOf(props.getProperty("debug", "false")));
            detectLoop.setSelected(Boolean.valueOf(props.getProperty("detect.loop", "true")));
            delay.getValueFactory().setValue(Integer.parseInt(props.getProperty("delay", "1000")));
            stepCount.getValueFactory().setValue(Integer.parseInt(props.getProperty("step.count", "3")));
            stepBackCount.getValueFactory().setValue(Integer.parseInt(props.getProperty("step.back.count", "3")));
            initialState.setText(props.getProperty("initial.state", INITIAL_STATE));
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
    }

    void setTape(String tape) {
        this.tapeInput.setText(tape);
    }

    void setWordMode() {
        machine.setSingleCharacter(false);
        wordMode.setSelected(true);
    }

    private void disableStepButtons(boolean disable) {
        playPause.setDisable(disable);
        step.setDisable(disable);
        stepMore.setDisable(disable);
    }

    void setLeftLimit() {
        this.leftLimit.setSelected(true);
    }

    void setRightLimit() {
        this.rightLimit.setSelected(true);
    }

    @Override
    protected void finalize() throws Throwable {
        if (runner != null) {
            runner.stop();
        }
        super.finalize();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        operatingMachine.setTapePanels(machineTape, operatingTape, transitionTable);
        machineTapeScrollPane.hvalueProperty().bindBidirectional(operatingTapeScrollPane.hvalueProperty());
        machine.tapeProperty().bind(tapeInput.textProperty());
        machine.leftLimitProperty().bind(leftLimit.selectedProperty());
        machine.rightLimitProperty().bind(rightLimit.selectedProperty());
        moveInput.getItems().addAll(Move.values());
        moveDefault.getItems().addAll(Move.values());
        moveDefault.setValue(PRAWO);
        clearMachineButton.disableProperty().bind(enableClearAll.selectedProperty().not());
        TableFilter<Transition> filter = TableFilter.forTableView(transitionTable).apply();
        machine.setTransitions(filter.getBackingList());
        transitionTable.setRowFactory(param -> {
            TableRow<Transition> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            ObservableList<MenuItem> items = contextMenu.getItems();
            MenuItem deleteTransition = new MenuItem("Usuń przejście");
            deleteTransition.setOnAction(event -> {
                Transition item = row.getItem();
                machine.removeTransition(item.getFrom(), item.getWhen());
            });
            items.add(deleteTransition);
//            Transition item = row.getItem();
//            if (item != null && INITIAL_STATE.equals(item.getFrom()) && machine.getState(INITIAL_STATE).getTransitionMap().size() <= 1) {
//                deleteTransition.setDisable(true);
//            } else {
//                deleteTransition.setDisable(false);
//            }
            MenuItem deleteState = new MenuItem("Usuń stan");
            deleteState.setOnAction(event -> machine.removeState(row.getItem().getFrom()));
            items.add(deleteState);
//            if (item != null && INITIAL_STATE.equals(item.getFrom())) {
//                deleteState.setDisable(true);
//            } else {
//                deleteState.setDisable(false);
//            }
            row.setContextMenu(contextMenu);
            return row;
        });
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        toColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        toColumn.setOnEditCommit(event -> {
            String toState = event.getNewValue();
            event.getRowValue().to(toState);
            machine.addState(toState);
        });
        moveColumn.setCellValueFactory(new PropertyValueFactory<>("move"));
        moveColumn.setCellFactory(param -> new TextFieldTableCell<>(new StringConverter<Move>() {
            @Override
            public String toString(Move move) {
                return move.toString();
            }

            @Override
            public Move fromString(String move) {
                return Move.parse(move);
            }
        }));
        whenColumn.setCellValueFactory(new PropertyValueFactory<>("when"));
        whenColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        thenColumn.setCellValueFactory(new PropertyValueFactory<>("then"));
        thenColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        stepCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        stepBackCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        delay.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 10000, 1000, 100));
        operatingMachine.debugModeProperty().bind(debug.selectedProperty());
        operatingMachine.stepTimeProperty().bind(delay.valueProperty());
        initializeRunningElements();
        operatingMachine.manualProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (!newValue) {
                    playPause.setText("Zatrzym");
                    disableButtons(true);
                } else {
                    playPause.setText("Uruchom");
                    disableButtons(false);
                }
            });
        });
        addTransition.disableProperty().bind(Bindings.createBooleanBinding(
                () -> fromInput.getText().trim().isEmpty(), fromInput.textProperty())
                .and(Bindings.createBooleanBinding(
                        () -> fromDefault.getText().trim().isEmpty(), fromDefault.textProperty()))
                .or(Bindings.createBooleanBinding(
                        () -> whenInput.getText().isEmpty(), whenInput.textProperty())
                        .and(Bindings.createBooleanBinding(
                                () -> whenDefault.getText().isEmpty(), whenDefault.textProperty()))));
        stepBack.disableProperty().bind(operatingMachine.emptyHistoryProperty());
        stepBackMore.disableProperty().bind(operatingMachine.emptyHistoryProperty());
        tapeInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                disableStepButtons(true);
            } else {
                disableStepButtons(false);
            }
        });
        disableStepButtons(true);
        tapeInput.textProperty().addListener(tapeListener);
        leftLimit.selectedProperty().addListener(tapeListener);
        rightLimit.selectedProperty().addListener(tapeListener);
        operatingMachine.detectLoopProperty().bind(detectLoop.selectedProperty());
        clearMachine();
        initialState.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                return;
            }
            setInitialState(newValue);
        });
    }

    void setInitialState(String newValue) {
        MachineState state = machine.getState(newValue);
        if (state == null) {
            machine.addState(newValue);
        }
        INITIAL_STATE = newValue;
        if (runner == null) {
            operatingMachine.setInitialState(newValue);
        }
    }

    @SuppressWarnings("WeakerAccess")
    private class TapeListener implements InvalidationListener {
        public void invalidate() {
            invalidated(null);
        }

        @Override
        public void invalidated(Observable observable) {
            if (runner == null) {
                operatingMachine.initialize();
            }
        }
    }
}
