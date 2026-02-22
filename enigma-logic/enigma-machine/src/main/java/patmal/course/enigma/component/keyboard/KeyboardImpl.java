package patmal.course.enigma.component.keyboard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class KeyboardImpl implements Keyboard, Serializable {
    private final String alphabetString;
    private final Map<Character, Integer> mapFromCharToInt;
    private final Map<Integer, Character> mapFromIntToChar;

    public KeyboardImpl(String alphabetString, Map<Character, Integer> mapFromCharToInt) {
        this.alphabetString = alphabetString.trim();
        this.mapFromCharToInt = mapFromCharToInt;
        this.mapFromIntToChar = createIntToCharMap();
    }

    private Map<Integer, Character> createIntToCharMap() {
        Map<Integer, Character> intToCharMap = new HashMap<>();
        for (Map.Entry<Character, Integer> entry : mapFromCharToInt.entrySet()) {
            intToCharMap.put(entry.getValue(), entry.getKey());
        }
        return intToCharMap;
    }

    @Override
    public Map<Integer, Character> getMapFromIntToChar() {
        return mapFromIntToChar;
    }

    @Override
    public Map<Character, Integer> getMapFromCharToInt() {
        return mapFromCharToInt;
    }

    @Override
    public int charToIndex(char c) {
        return mapFromCharToInt.get(c);
    }

    @Override
    public char indexToChar(int index) {
        return mapFromIntToChar.get(index);
    }

    @Override
    public int getAlphabetLength() {
        return alphabetString.length();
    }

    @Override
    public boolean isValidChar(char c) {
        return alphabetString.indexOf(c) != -1;
    }

    @Override
    public String toString() { return alphabetString; }

}
