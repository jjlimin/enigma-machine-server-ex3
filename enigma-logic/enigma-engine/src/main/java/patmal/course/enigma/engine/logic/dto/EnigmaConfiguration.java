package patmal.course.enigma.engine.logic.dto;

import java.io.Serializable;
import java.util.*;

public class EnigmaConfiguration implements Serializable {
    private final List<Integer> rotorIDs;
    private final List<RotorLetterAndNotch> rotorLetterAndNotch;
    private final String reflectorID;
    private final Map<Character, Character> plugBoardMapping;
    private final List<EnigmaMessage> messages;

    public EnigmaConfiguration(List<Integer> rotorIDs, List<RotorLetterAndNotch> rotorLetterAndNotch, String reflectorID, Map<Character, Character> plugboardMapping) {
        this.rotorIDs = rotorIDs;
        this.rotorLetterAndNotch = rotorLetterAndNotch;
        this.reflectorID = reflectorID;
        this.plugBoardMapping = plugboardMapping;
        this.messages = new ArrayList<>();
    }

    public void addMessage(String input, String output, long time){
        this.messages.add(new EnigmaMessage(input, output, time));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EnigmaConfiguration that = (EnigmaConfiguration) o;
        return Objects.equals(rotorIDs, that.rotorIDs) && Objects.equals(rotorLetterAndNotch, that.rotorLetterAndNotch) && Objects.equals(reflectorID, that.reflectorID) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rotorIDs, rotorLetterAndNotch, reflectorID, messages);
    }

    public Map<Character, Character> getPlugBoardMapping() {
        return plugBoardMapping;
    }

    public String getReflectorID() {
        return reflectorID;
    }

    public List<RotorLetterAndNotch> getRotorLetterAndNotch() {
        return rotorLetterAndNotch;
    }

    public List<Integer> getRotorIDs() {
        return rotorIDs;
    }

    public List<EnigmaMessage> getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        StringBuilder configStr = new StringBuilder();
        appendRotorsToStr(configStr);
        appendRotorsAndNotchesToStr(configStr);
        appendReflectorToStr(configStr);
        appendPlugBoardToStr(configStr);
        return configStr.toString();
    }

    private void appendPlugBoardToStr(StringBuilder configStr) {

        List<Character> keys = new ArrayList<>(this.plugBoardMapping.keySet());
        Collections.sort(keys);

        List<String> pairStrings = new ArrayList<>();
        Set<Character> processed = new HashSet<>();

        for (char key : keys) {
            if (processed.contains(key)) {
                continue;
            }
            char value = this.plugBoardMapping.get(key);
            
            // Add both to processed so we don't print the reverse pair
            processed.add(key);
            processed.add(value);

            // Append the pair in the A|B format
            pairStrings.add(key + "|" + value);
        }

        String joinedPairs = String.join(",", pairStrings);

        configStr.append("<");
        configStr.append(joinedPairs);
        configStr.append(">");
    }

    private void appendReflectorToStr(StringBuilder ConfigStr) {

        ConfigStr.append("<");
        ConfigStr.append(this.getReflectorID());
        ConfigStr.append(">");
    }

    private void appendRotorsAndNotchesToStr(StringBuilder ConfigStr) {

        ConfigStr.append("<");

        for(int i = 0; i < this.getRotorLetterAndNotch().size(); i++) {
            ConfigStr.append(this.getRotorLetterAndNotch().get(i).getLetter());
            ConfigStr.append("(");
            ConfigStr.append(this.getRotorLetterAndNotch().get(i).getNotchPos());
            ConfigStr.append(")");
            if(i != this.getRotorLetterAndNotch().size() - 1){
                ConfigStr.append(",");
            }
        }

        ConfigStr.append(">");
    }

    private void appendRotorsToStr(StringBuilder ConfigStr) {

        ConfigStr.append("<");
        for (int i = 0; i < this.getRotorIDs().size(); i++) {
            ConfigStr.append(this.getRotorIDs().get(i));
            if(i != this.getRotorIDs().size() - 1){
                ConfigStr.append(",");
            }
        }

        ConfigStr.append(">");
    }
}
