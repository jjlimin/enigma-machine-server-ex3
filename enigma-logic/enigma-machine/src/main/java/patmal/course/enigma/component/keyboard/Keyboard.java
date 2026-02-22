package patmal.course.enigma.component.keyboard;

import java.util.Map;

public interface Keyboard {
    Map<Integer, Character> getMapFromIntToChar();
    Map<Character, Integer> getMapFromCharToInt();

    int charToIndex(char c);
    char indexToChar(int index);
    int getAlphabetLength();

    boolean isValidChar(char c);
}
