package de.ovgu.softwareprojekt.servers.nes;

/**
 * Psychic button ids
 */
enum PsychicNesButton {
    A_BUTTON(0),
    B_BUTTON(1),
    X_BUTTON(2),
    Y_BUTTON(3),
    SELECT_BUTTON(4),
    START_BUTTON(5),
    R_BUTTON(6),
    L_BUTTON(7),
    LEFT_BUTTON(8),
    UP_BUTTON(9),
    RIGHT_BUTTON(10),
    DOWN_BUTTON(11);

    /**
     * The integer representation of this key used for communication with the app
     */
    private final int psychicButtonId;

    /**
     * This is a private constructor, used for assigning each of the psychic buttons a fixed id
     *
     * @param psychicId the integer used for representing this key in the app
     */
    PsychicNesButton(int psychicId) {
        this.psychicButtonId = psychicId;
    }

    /**
     * Map the enum to the integer used in the app
     */
    public int psychicId() {
        return psychicButtonId;
    }
}