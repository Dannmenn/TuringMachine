package pl.mendroch.uj.turing.model;

public enum Move {
    LEWO,
    BEZ,
    PRAWO;

    public static Move parse(String moveString) {
        switch (moveString.charAt(0)) {
            case 'l':
            case 'L':
                return LEWO;
            case 'p':
            case 'P':
                return PRAWO;
        }
        return BEZ;
    }

    public Move revert() {
        if (this == LEWO) {
            return PRAWO;
        }
        if (this == PRAWO) {
            return LEWO;
        }
        return BEZ;
    }

    public int move(int actualPosition) {
        if (this == LEWO) {
            return --actualPosition;
        }
        if (this == PRAWO) {
            return ++actualPosition;
        }
        return actualPosition;
    }
}
