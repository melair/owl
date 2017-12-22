package net.melaircraft.owl.library;

import net.melaircraft.owl.library.exception.drive.InvalidDriveException;
import net.melaircraft.owl.library.exception.slot.InactivateSlotException;
import net.melaircraft.owl.library.exception.slot.InvalidSlotException;
import net.melaircraft.owl.library.exception.slot.LockedSlotException;
import net.melaircraft.owl.library.exception.slot.NoStorageSlotException;
import net.melaircraft.owl.library.exception.slot.ResizeWouldTruncateSlotException;

/**
 * A disk bundle (a MMB file).
 */
public interface DiskBundle {
    /** Header flag indicating disk is writable. */
    byte HEADER_FLAG_DISK_WRITEABLE = (byte) 0x0f;
    /** Header flag indicating disk is unformatted. */
    byte HEADER_FLAG_DISK_UNFORMATTED = (byte) 0xf0;
    /** Sector size on DFS disk. */
    int SECTOR_SIZE = 256;
    /** Sectors per track on disk. */
    int SECTORS_PER_TRACK = 10;
    /** Number of tracks on a side of a disk. */
    int TRACKS_PER_DISK = 80;
    /** Total number of bytes for a disk. */
    int DISK_SIZE = SECTOR_SIZE * SECTORS_PER_TRACK * TRACKS_PER_DISK;
    /** Number of sectors at start of MMB for header and catalogue. */
    int MMB_SECTOR_COUNT = 32;
    /** Offset until first disk. */
    int INITIAL_OFFSET = SECTOR_SIZE * MMB_SECTOR_COUNT;
    /** Offset in header for flags. */
    int HEADER_FLAG_OFFSET = 15;
    /** Maximum length of a disk title. */
    int MAXIMUM_DISK_TITLE = 12;

    /**
     * Set the slot which is allocated to the drive number when computer is started.
     *
     * @param drive drive number (0 - 3)
     * @param slot slot number (0 - 510)
     * @throws InvalidDriveException if the drive number provided is not valid
     * @throws InvalidSlotException if the slot number provided is not valid
     */
    void setBootSlot(int drive, int slot) throws InvalidDriveException, InvalidSlotException;

    /**
     * Get the slot which is allocated to the provided drive number when computer is started.
     *
     * @param drive drive number (0 - 3)
     * @return slot number (0 - 510)
     * @throws InvalidDriveException if the drive number provided is not valid
     */
    int getBootSlot(int drive) throws InvalidDriveException;

    /**
     * Check to see if a slot is currently occupied.
     *
     * @param slot slot number (0 - 510)
     * @return true if the slot is occupied
     * @throws InvalidSlotException if the slot number provided is not valid
     */
    boolean isOccupied(int slot) throws InvalidSlotException;

    /**
     * Activate an unused slot from the bundle, this will mark it in use and be available.
     *
     * <b>Warning</b>: This may cause issues of the image has been corrupted, wiped or was never allocated previously.
     * If previously unused then the slot will need to be formatted before use.
     *
     * @param slot slot number (0 - 510)
     * @throws InvalidSlotException if the slot number provided is not valid
     * @throws NoStorageSlotException if the slot number has no storage allocated
     */
    void activate(int slot) throws InvalidSlotException, NoStorageSlotException;

    /**
     * Deactivate a disk slot from the catalog.
     *
     * @param slot slot number (0 - 510)
     * @param wipe true if the data in the bundle should be wiped to 0x00
     * @throws InvalidSlotException if the slot number provided is not valid
     * @throws LockedSlotException if the slot is in used and currently locked
     */
    void deactivate(int slot, boolean wipe) throws InvalidSlotException, LockedSlotException;

    /**
     * Extract a disk image out from the bundle, the original disk will be left in place and unmodified.
     *
     * @param slot slot number (0 - 510)
     * @return the extracted disk image
     * @throws InvalidSlotException if the slot number provided is not valid
     * @throws InactivateSlotException if the slot is not currently active
     * @throws NoStorageSlotException if the slot is allocated but there is no storage allocated
     */
    Disk extract(int slot) throws InvalidSlotException, InactivateSlotException, NoStorageSlotException;

    /**
     * Insert a disk image into the bundle, any disk image already present will be wiped. In the case a smaller disk
     * is inserted remaining space will be wiped to 0x00. An insert into an unused slot will activate the slot.
     *
     * @param slot slot number (0 - 510)
     * @param disk disk image to insert
     * @throws InvalidSlotException if the slot number provided is not valid
     * @throws LockedSlotException if the slot is in used and currently locked
     */
    void insert(int slot, Disk disk) throws InvalidSlotException, LockedSlotException;

    /**
     * Check to see if the slot is marked as write protected / locked.
     *
     * @param slot slot nuber (0 - 510)
     * @return true if the slot is write protected
     * @throws InvalidSlotException if the slot number provided is not valid
     * @throws InactivateSlotException if the slot is not currently active
     */
    boolean isLocked(int slot) throws InvalidSlotException, InactivateSlotException;

    /**
     * Mark the slot as write protected / locked.
     *
     * @param slot slot number (0 - 510)
     * @throws InvalidSlotException if the slot number provided is not valid
     * @throws InactivateSlotException if the slot is not currently active
     */
    void lock(int slot) throws InvalidSlotException, InactivateSlotException;

    /**
     * Mark the slot as write allowed / unlocked.
     *
     * @param slot slot number (0 - 510)
     * @throws InvalidSlotException if the slot number provided is not valid
     * @throws InactivateSlotException if the slot is not currently active
     */
    void unlock(int slot) throws InvalidSlotException, InactivateSlotException;

    /**
     * Get the name of a slot.
     *
     * @param slot slot number (0 - 510)
     * @return name of slot
     * @throws InvalidSlotException if the slot number provided is not valid
     */
    String getName(int slot) throws InvalidSlotException;

    /**
     * Change the name of a slot in the catalogue.
     *
     * @param slot slot number (0 - 510)
     * @param name new game for slot, up to 12 characters
     * @throws InvalidSlotException if the slot number provided is not valid
     */
    void setName(int slot, String name) throws InvalidSlotException;

    /**
     * Get the current storage size allocated for this bundle in slots.
     *
     * @return number of slots available for storage in bundle
     */
    int getStorageSize();

    /**
     * Change the storage size of the bundle up to the number of slots provided.
     *
     * @param slot slot number (0 - 511)
     * @throws InvalidSlotException if the slot number provided is not valid
     * @throws ResizeWouldTruncateSlotException if the new size would truncate an existing in use slot
     */
    void changeStorageSize(int slot) throws InvalidSlotException, ResizeWouldTruncateSlotException;
}
