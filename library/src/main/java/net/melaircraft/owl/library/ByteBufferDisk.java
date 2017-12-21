package net.melaircraft.owl.library;

import java.nio.ByteBuffer;

/**
 * A byte buffered backing for a disk.
 */
public class ByteBufferDisk implements Disk {
    /** Raw image bytes. */
    private final byte[] imageBytes;
    /** ByteBuffer wrapping of bytes. */
    private final ByteBuffer image;

    /**
     * Construct a new blank disk.
     */
    public ByteBufferDisk() {
        imageBytes = new byte[DiskBundle.DISK_SIZE];
        image = ByteBuffer.wrap(imageBytes);
    }

    /**
     * Construct a new disk, sourcing bytes from array.
     *
     * @param sourceBytes source bytes array
     */
    public ByteBufferDisk(byte[] sourceBytes) {
        this();

        for (int i = 0; i < DiskBundle.DISK_SIZE && i < sourceBytes.length; i++) {
            image.put(sourceBytes[i]);
        }
    }

    @Override
    public byte[] getImage() {
        return imageBytes;
    }

    /**
     * Get a mutable byte buffer for this disk image.
     *
     * @return disk byte buffer
     */
    public ByteBuffer getByteBuffer() {
        return image;
    }
}
