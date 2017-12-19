package net.melaircraft.owl.library.excpetion.drive;

/**
 * Exception to indicate that a drive is invalid.
 */
public final class InvalidDriveException extends DriveException {
    /**
     * Construct a new invalid drive exception.
     *
     * @param drive drive number
     */
    public InvalidDriveException(int drive) {
        super(drive, "Drive number " + drive + " is invalid.");
    }
}
