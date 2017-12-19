package net.melaircraft.owl.library.excpetion;

/**
 * Exception to indicate that a slot is inactive and has no data.
 */
public final class InactivateSlotException extends SlotException {
    /**
     * Construct a new inactive slot exception.
     *
     * @param slot slot number
     */
    public InactivateSlotException(int slot) {
        super(slot, "Slot number " + slot + " is inactive.");
    }
}
