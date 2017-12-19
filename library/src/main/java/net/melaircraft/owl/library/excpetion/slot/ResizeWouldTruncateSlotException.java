package net.melaircraft.owl.library.excpetion.slot;

/**
 * Exception to indicate a bundle resize would truncate slots.
 */
public final class ResizeWouldTruncateSlotException extends SlotException {
    /**
     * Construct a new bundel resize would truncate slots exception.
     */
    public ResizeWouldTruncateSlotException(int slot) {
        super(slot, "Resizing the bundle would truncate slots, lowest effected is " + slot + ".");
    }
}
