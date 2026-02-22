package patmal.course.enigma.engine.logic.dto;

import java.io.Serializable;

public class EnigmaMessage implements Serializable {
    private final String input;
    private final String output;
    private final long time;

    public EnigmaMessage(String input, String output, long time) {
        this.input = input;
        this.output = output;
        this.time = time;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "<" + input + ">" + " --> " + "<" + output + ">" + " (" + time + " nano-seconds)";
    }

}
