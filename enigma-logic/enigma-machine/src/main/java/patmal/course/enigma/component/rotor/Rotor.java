package patmal.course.enigma.component.rotor;

import patmal.course.enigma.component.keyboard.Keyboard;

import java.util.List;

public interface Rotor {
    void setPosition(int position);
    int encodeForward(int entryLocation);
    int encodeBackward(int entryLocation);
    int getNotchPosition();
    void rotate();
    int getTopLetter();

    void setId(int id);
    int getId();
    void setKeyboard(Keyboard keyboard);

    List<Integer> getRightColumn();
    List<Integer> getLeftColumn();

    Keyboard getKeyboard();

    Rotor cloneRotor();
}