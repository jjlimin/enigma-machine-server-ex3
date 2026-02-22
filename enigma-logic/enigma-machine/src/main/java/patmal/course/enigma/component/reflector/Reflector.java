package patmal.course.enigma.component.reflector;

import patmal.course.enigma.component.keyboard.Keyboard;

public interface Reflector {
    int reflect(int inputIndex);
    void setKeyboard(Keyboard keyboard);
    String getId();
}
