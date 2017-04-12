package pl.mendroch.uj.turing.view;

import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.java.Log;
import org.controlsfx.dialog.ExceptionDialog;
import pl.mendroch.uj.turing.model.Move;
import pl.mendroch.uj.turing.model.Transition;
import pl.mendroch.uj.turing.model.TuringMachine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pl.mendroch.uj.turing.model.FontStyle.FONT;
import static pl.mendroch.uj.turing.model.TuringMachineConstants.END_CHARACTER;
import static pl.mendroch.uj.turing.model.TuringMachineConstants.INITIAL_STATE;

@Log
class FileDialog {
    private static final Pattern MODE = Pattern.compile("tryb wyrazu");
    private static final Pattern TAPE = Pattern.compile("taśma:(#?[a-zA-Z0-9]+#?)");
    private static final Pattern INITIAL = Pattern.compile("stan początkowy:(.+)");
    private static final Pattern TRANSITION = Pattern.compile("(.+)->(.+):(.+)->(.+):([a-zA-Z]+)");
    private static File openedFile;
    private static File savedFile;

    static void openFile(MainWindowController controller) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otwórz plik z maszyną Turinga");
        if (openedFile != null) {
            fileChooser.setInitialDirectory(openedFile);
        } else if (savedFile != null) {
            fileChooser.setInitialDirectory(savedFile);
        }
        ObservableList<FileChooser.ExtensionFilter> extensionFilters = fileChooser.getExtensionFilters();
        extensionFilters.clear();
        extensionFilters.add(new FileChooser.ExtensionFilter("Pliki tekstowe", "*.txt"));
        File file = fileChooser.showOpenDialog(MainWindowController.getStage());
        if (file != null) {
            openedFile = file.getParentFile();
            controller.clearMachine();
            loadMachineFromFile(controller, file);
        }
    }

    static void loadMachineFromFile(MainWindowController controller, File file) {
        try {
            TuringMachine machine = controller.getMachine();
            Files.lines(file.toPath()).forEach(line -> {
                Matcher modeMatcher = MODE.matcher(line.toLowerCase());
                if (modeMatcher.matches()) {
                    controller.setWordMode();
                    return;
                }
                Matcher initialMatcher = INITIAL.matcher(line);
                if (initialMatcher.matches()) {
                    controller.setInitialState(initialMatcher.group(1));
                    return;
                }
                Matcher transitionMatcher = TRANSITION.matcher(line);
                if (transitionMatcher.matches()) {
                    machine.addTransition(new Transition()
                            .from(transitionMatcher.group(1))
                            .to(transitionMatcher.group(2))
                            .when(transitionMatcher.group(3))
                            .then(transitionMatcher.group(4))
                            .move(Move.parse(transitionMatcher.group(5)))
                    );
                    return;
                }
                Matcher tapeMatcher = TAPE.matcher(line);
                if (!tapeMatcher.matches()) {
                    return;
                }
                String tapeString = tapeMatcher.group(1);
                if (tapeString.trim().isEmpty()) {
                    return;
                }
                if (END_CHARACTER.equals(tapeString.charAt(0) + "")) {
                    controller.setLeftLimit();
                    tapeString = tapeString.substring(1);
                }
                if (END_CHARACTER.equals(tapeString.charAt(tapeString.length() - 1) + "")) {
                    controller.setRightLimit();
                    tapeString = tapeString.substring(0, tapeString.length() - 1);
                }
                controller.setTape(tapeString);
            });
        } catch (IOException e) {
            log.warning(e.getMessage());
            showExceptionDialog(MainWindowController.getStage(), e);
        }
    }

    private static void showExceptionDialog(Window stage, IOException e) {
        ExceptionDialog exceptionDialog = new ExceptionDialog(e);
        exceptionDialog.initOwner(stage);
        exceptionDialog.getDialogPane().setStyle(FONT);
        exceptionDialog.show();
    }

    static void saveToFile(MainWindowController controller) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz maszynę Turinga do pliku");
        if (savedFile != null) {
            fileChooser.setInitialDirectory(savedFile);
        } else if (openedFile != null) {
            fileChooser.setInitialDirectory(openedFile);
        }
        ObservableList<FileChooser.ExtensionFilter> extensionFilters = fileChooser.getExtensionFilters();
        extensionFilters.clear();
        extensionFilters.add(new FileChooser.ExtensionFilter("Pliki tekstowe", "*.txt"));
        File file = fileChooser.showSaveDialog(MainWindowController.getStage());
        if (file != null) {
            savedFile = file.getParentFile();
            saveMachineToFile(MainWindowController.getStage(), controller.getMachine(), file);
        }
    }

    static void saveMachineToFile(Stage stage, TuringMachine machine, File file) {
        log.info("saving machine state to file");
        try {
            Files.write(file.toPath(), getMachineStates(machine));
        } catch (IOException e) {
            log.warning(e.getMessage());
            showExceptionDialog(stage, e);
        }
        log.info("machine state saved");
    }

    private static List<String> getMachineStates(TuringMachine machine) {
        List<String> output = new ArrayList<>();
        output.add(machine.isSingleCharacter() ? "Tryb pojedynczych znaków" : "tryb wyrazu");
        output.add("taśma:" + machine.getTapeString());
        output.add("stan początkowy:" + INITIAL_STATE);
        SortedList<Transition> transitions = machine.getTransitions()
                .sorted(Comparator.comparing(Transition::getWhen))
                .sorted(Comparator.comparing(Transition::getFrom));
        for (Transition transition : transitions) {
            String transitionBuilder = transition.getFrom() + "->" + transition.getTo() + ":" +
                    transition.getWhen() + "->" + transition.getThen() + ":" +
                    transition.getMove().toString();
            output.add(transitionBuilder);
        }
        return output;
    }

    static File getOpenedFile() {
        return openedFile;
    }

    static void setOpenedFile(String file) {
        if (file != null && Files.exists(Paths.get(file))) {
            openedFile = new File(file);
        }
    }

    static File getSavedFile() {
        return savedFile;
    }

    static void setSavedFile(String file) {
        if (file != null && Files.exists(Paths.get(file))) {
            savedFile = new File(file);
        }
    }
}
