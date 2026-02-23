package patmal.course.enigma.core.formatter;

import org.springframework.stereotype.Component;
import patmal.course.enigma.component.keyboard.Keyboard;
import patmal.course.enigma.engine.logic.repository.Repository;
import patmal.course.enigma.session.EnigmaSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EnigmaFormatter {
    public String formatPositions(List<Integer> rotorIds, List<Character> positions, Repository catalog) {
        if (positions == null || positions.isEmpty() || rotorIds == null) {
            return "";
        }

        List<String> formatted = new ArrayList<>();
        Keyboard keyboard = catalog.getKeyboard();
        int alphabetSize = keyboard.getAlphabetLength();

        for (int i = 0; i < positions.size(); i++) {
            int id = rotorIds.get(i);
            char pos = positions.get(i);

            // 1. Get the actual rotor component to find its notch position
            var rotorComponent = catalog.getAllRotors().get(id);
            int currentIdx = keyboard.charToIndex(pos);
            int notchIdx = rotorComponent.getNotchPosition() + 1;

            // 2. Calculate steps until notch: (Notch - Current + Size) % Size
            // This gives the distance from the current position to the notch
            int distanceToNotch = (notchIdx - currentIdx + alphabetSize) % alphabetSize;

            // 3. Format as "Char(Distance)"
            formatted.add(String.format("%c(%d)", pos, distanceToNotch));
        }

        return String.join(",", formatted);
    }

    public String formatFullOriginalCode(EnigmaSession session, Repository catalog) {
        // 1. Format Rotor IDs: <1,2,3>
        String ids = session.getCurrentRotorIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "<", ">"));

        // 2. Format Initial Positions: <A(0),B(1)>
        String pos = formatPositions(session.getCurrentRotorIds(), session.getOriginalPositions(), catalog);
        pos = "<" + pos + ">";

        // 3. Format Reflector: <III>
        String reflector = "<" + session.getCurrentReflectorId() + ">";

        // 4. Format Plugs (if any): <A|Z,B|Y>
        String plugs = session.getCurrentPlugs().entrySet().stream()
                .filter(e -> e.getKey() < e.getValue()) // Avoid duplicates like A|Z and Z|A
                .map(e -> e.getKey() + "|" + e.getValue())
                .collect(Collectors.joining(",", "<", ">"));

        return ids + pos + reflector + (plugs.equals("<>") ? "" : plugs);
    }
}
