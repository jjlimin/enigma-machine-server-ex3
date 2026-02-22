package patmal.course.enigma.component.reflector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RomanValues implements Serializable {
    public static Map<String, Boolean> romanValues = new HashMap<>();
    public RomanValues() {
        romanValues.put("I", false);
        romanValues.put("II", false);
        romanValues.put("III", false);
        romanValues.put("IV", false);
        romanValues.put("V", false);
    }

    public static void markAsUsed(String id){
        romanValues.put(id, true);
    }

    public static Boolean checkIfUsed(String id){
        return romanValues.get(id);
    }

    public static void clear() {
        romanValues.put("I", false);
        romanValues.put("II", false);
        romanValues.put("III", false);
        romanValues.put("IV", false);
        romanValues.put("V", false);
    }
}
