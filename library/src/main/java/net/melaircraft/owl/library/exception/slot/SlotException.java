package net.melaircraft.owl.library.exception.slot;

/**
 * An exception which relates to a bundles slot.
 */
public abstract class SlotException extends RuntimeException {
    /** Slot number which the exception is regarding. */
    private final int slot;

    /**
     * Construct a new SlotException.
     *
     * @param slot slot the exception is about
     * @param message message of exception
     */
    protected SlotException(int slot, String message) {
        super(message);
        this.slot = slot;
    }

    /**
     * Get the slot number this exception is about.
     *
     * @return slot number
     */
    public int getSlot() {
        return slot;
    }
}
