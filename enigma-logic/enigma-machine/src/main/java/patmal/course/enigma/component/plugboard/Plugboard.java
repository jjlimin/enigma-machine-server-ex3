package patmal.course.enigma.component.plugboard;

import java.util.Map;

public interface Plugboard {
    char substitute(char inputChar);

    Map<Character, Character> getWiringMap();
}
