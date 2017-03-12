package pl.mendroch.uj.turing.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@SuppressWarnings({"unused", "WeakerAccess", "SimplifiableIfStatement"})
public class Transition {
    private final SimpleStringProperty from = new SimpleStringProperty();
    private final SimpleStringProperty to = new SimpleStringProperty();
    private final ObjectProperty<Move> move = new SimpleObjectProperty<>();
    private final SimpleStringProperty when = new SimpleStringProperty();
    private final SimpleStringProperty then = new SimpleStringProperty();

    public String getFrom() {
        return from.get();
    }

    public Transition from(String from) {
        this.from.set(from);
        return this;
    }

    public SimpleStringProperty fromProperty() {
        return from;
    }

    public String getTo() {
        return to.get();
    }

    public Transition to(String to) {
        this.to.set(to);
        return this;
    }

    public SimpleStringProperty toProperty() {
        return to;
    }

    public Move getMove() {
        return move.get();
    }

    public Transition move(Move move) {
        this.move.set(move);
        return this;
    }

    public ObjectProperty<Move> moveProperty() {
        return move;
    }

    public String getWhen() {
        return when.get();
    }

    public Transition when(String when) {
        this.when.set(when);
        return this;
    }

    public SimpleStringProperty whenProperty() {
        return when;
    }

    public String getThen() {
        return then.get();
    }

    public Transition then(String then) {
        this.then.set(then);
        return this;
    }

    public SimpleStringProperty thenProperty() {
        return then;
    }

    public boolean deepEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transition that = (Transition) o;

        if (!from.get().equals(that.from.get())) return false;
        if (!to.get().equals(that.to.get())) return false;
        if (!move.get().equals(that.move.get())) return false;
        if (!when.get().equals(that.when.get())) return false;
        return then.get().equals(that.then.get());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transition that = (Transition) o;

        if (!from.get().equals(that.from.get())) return false;
        return when.get().equals(that.when.get());
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + when.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return from.get() + "->" + to.get() + ":" + when.get() + ":" + then.get() + ":" + move.get();
    }
}
