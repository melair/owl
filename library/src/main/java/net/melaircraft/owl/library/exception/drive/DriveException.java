package net.melaircraft.owl.library.exception.drive;

/**
 * An exception which relates to a drive.
 */
public abstract class DriveException extends RuntimeException {
    /** Drive number which the exception is regarding. */
    private final int drive;

    /**
     * Construct a new DriveException.
     *
     * @param drive drive the exception is about
     * @param message message of exception
     */
    protected DriveException(int drive, String message) {
        super(message);
        this.drive = drive;
    }

    /**
     * Get the drive number this exception is about.
     *
     * @return drive number
     */
    public int getDrive() {
        return drive;
    }
}
