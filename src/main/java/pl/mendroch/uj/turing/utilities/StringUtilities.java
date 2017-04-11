package pl.mendroch.uj.turing.utilities;

import java.util.LinkedList;

public class StringUtilities {

    private StringUtilities() {
        //Hide implicit constructor
    }

    public static LinkedList<String> getListFromString(String string, boolean singleCharacter) {
        LinkedList<String> list = new LinkedList<>();
        if (singleCharacter) {
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
        return list;
    }
}
