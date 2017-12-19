package net.melaircraft.owl.library.excpetion;

/**
 * Exception to indicate that a slot is invalid.
 */
public final class InvalidSlotException extends SlotException {
    /**
     * Construct a new invalid slot exception.
     *
     * @param slot slot number
     */
    public InvalidSlotException(int slot) {
        super(slot, "Slot number " + slot + " is invalid.");
    }
}
