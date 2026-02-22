package patmal.course.enigma.machine;

import patmal.course.enigma.component.rotor.RotorManager;

public interface Machine  {
    char encryptChar(char inputChar);

    RotorManager getRotorManager();
}
