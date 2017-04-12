package pl.mendroch.uj.turing.view;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static pl.mendroch.uj.turing.model.FontStyle.FONT;

@SuppressWarnings("WeakerAccess")
public class Dialog {
    private static Alert errorDialog;
    private static Alert warningDialog;

    public static void showError(String content) {
        if (errorDialog == null) {
            errorDialog = new Alert(ERROR);
            errorDialog.initOwner(MainWindowController.getStage());
        }
        if (!errorDialog.isShowing()) {
            showAlertDialog("Nie można przeprowadzić przejścia", content, errorDialog);
        }
    }

    public static void showWarning(String content) {
        if (warningDialog == null) {
            warningDialog = new Alert(WARNING);
            warningDialog.initOwner(MainWindowController.getStage());
        }
        if (!warningDialog.isShowing()) {
            showAlertDialog("Brak przejścia", content, warningDialog);
        }
    }

    private static void showAlertDialog(String title, String content, Alert alert) {
        Platform.runLater(() -> {
            alert.setTitle(title);
            alert.getDialogPane().setStyle(FONT);
            alert.setContentText(content);
            alert.show();
        });
    }

    public static void showAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(MainWindowController.getStage());
        alert.setTitle("Pomoc");
        alert.getDialogPane().setStyle(FONT);
        alert.setHeaderText(null);
        TextArea textArea = new TextArea();
        textArea.setText("Menu:\n" +
                "\tPlik:\n" +
                "\t\tOtwórz - otwórz maszynę turinga zapisaną w pliku tekstowym\n" +
                "\t\tZapisz - zapisz maszynę turinga do pliku tekstowego\n" +
                "\t\t\tPrzykładowy zapis maszyny Turinga w pliku tekstowym:\n" +
                "\t\t\t\ttryb wyrazu\n" +
                "\t\t\t\ttaśma:#pierwszeDrugie Trzecie#\n" +
                "\t\t\t\tq1->q2:a->b:prawo\n" +
                "\t\tZamknij - zamyka aplikację. Aktualnie używana maszyna wraz z ustawieniami aplikacji zostanie zapisana do plików konfiguracji i przywrócona przy następnym uruchomieniu\n" +
                "\tEdycja:\n" +
                "\t\tWykrywanie zapętleń - włączanie/wyłączanie wykrywania zapętleń i wyświetlanie powiadomień" +
                "\t\tWyczyść - czyści wszystkie pola wraz z aktualną maszyną Turingai przywraca domyślne ustawienia aplikacji\n" +
                "\tCzcionka - rozmiar czcionki w aplikacji\n" +
                "\tTyp taśmy:\n" +
                "\t\tPojedyncze znaki - każdy znak jest osobnym elementem alfabetu maszyny\n" +
                "\t\tZapis wyrazami - każdy wyraz zaczyna się wielką literą\n" +
                "\tPomoc:\n" +
                "\t\tOpis - opis aplikacji i ustawień\n\n" +
                "Wprowadzanie danych:" +
                "\tWprowadzanie taśmy:\n" +
                "\t\tWprowadzanie elementów taśmy odpowiednio do wybranego trybu. Pusty symbol zapisywać można jako ' ', jedynym zakazanym znakiem jest #.\n" +
                "\t\t\tprawostronne i lewostronne ograniczenie zostanie wprowadzone jako znak #\n" +
                "\t\t\ttaśma zostanie przeniesiona do taśmy aktualnie wykonywanej maszyny jedynie w przypadku gdy maszyna nie została jeszcze uruchomiona" +
                "\t\tWprowadzanie przejść. Stany użyte w przejściach jeśli nie istnieją zostaną automatycznie dodane do stanów maszyny. Pozostawione pola zostaną pobrane z domyślnego przejścia.\n" +
                "\t\tMożliwość wprowadzania wielu znaków('kiedy', 'wtedy') równocześnie na takiej samej zasadzie jak elementy taśmy. Znaki specjalne:\n" +
                "\t\t\t'*' - kiedy: dowolne dopasowanie, wtedy: przepisuje zastany element\n" +
                "\t\t\t' ' - znak pusty\n" +
                "\t\tStan początkowy - ustawia stan początkowy tylko w przypadku gdy maszyna nie została uruchomiona" +
                "\t\tczyszczenie pól po dodaniu - pola dodawania przejść zostaną wyczyszczone po dodaniu\n" +
                "Modyfikowanie przejść:\n" +
                "\tDane oprócz kolumny 'Ze stanu' mogą być modyfikowane bezpośrednio z poziomu tabeli. W menu kontekstowym wiersza można usunąć pojedynczy wiersz, a także wszystkie przejścia dla stanu z kolumny 'Ze stanu'\n" +
                "\tDane w tabeli można sortować i filtrować poprzez lewo i prawo klik na kolumnach tabeli\n" +
                "Uruchamianie maszyny:\n" +
                "\topóźnienie - aplikowane w przypadku uruchomienia automatycznych przejść przez przycisk 'Uruchom'. Pozostałe przyciski włączą tryb manualny\n" +
                "\ttryb debugowania - włączenie tego trybu spowoduje używanie aktualnych przejść maszyny widocznych w tabeli.\n" +
                "\t\tUmożliwia to szybkie zmiany działającej maszyny, lecz może spowodować nieprawidłowe przejścia wstecz. Możliwe również jest, że po zmianach aktualna pozycja na taśmie może nie być osiągalna\n" +
                "\tograniczenia są przedstawione przez znak # białym kolorem na czarnym tle\n" +
                "\taktualna pozycja przedstawiona jest przez biały znak na niebieskim tle\n");
        alert.setResizable(true);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(textArea);
        dialogPane.setStyle(FONT);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMinWidth(Region.USE_PREF_SIZE);
        dialogPane.setMaxWidth(Double.MAX_VALUE);
        dialogPane.setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}
