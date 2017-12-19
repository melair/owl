package net.melaircraft.owl.library;

/**
 * A disk.
 */
public interface Disk {
    /**
     * Get the disks sector count.
     *
     * @return number of sectors in the disk image
     */
    int getSectorCount();

    /**
     * Get the disks raw image.
     *
     * @return raw disk image
     */
    byte[] getImage();
}
