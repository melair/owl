package net.melaircraft.owl.library.excpetion;

/**
 * Exception to indicate that a slot is locked and can not be modified.
 */
public final class LockedSlotException extends SlotException {
    /**
     * Construct a new locked slot exception.
     *
     * @param slot slot number
     */
    public LockedSlotException(int slot) {
        super(slot, "Slot " + slot + " is currently write protected / locked.");
    }
}
