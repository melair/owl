package net.melaircraft.owl.library.exception.slot;

/**
 * Exception to indicate that a slot has no storage.
 */
public final class NoStorageSlotException extends SlotException {
    /**
     * Construct a new no storage slot exception.
     *
     * @param slot slot number
     */
    public NoStorageSlotException(int slot) {
        super(slot, "Slot number " + slot + " can not be used as there is no storage available.");
    }
}
