package net.melaircraft.owl.library;

import net.melaircraft.owl.library.exception.drive.InvalidDriveException;
import net.melaircraft.owl.library.exception.slot.InactivateSlotException;
import net.melaircraft.owl.library.exception.slot.InvalidSlotException;
import net.melaircraft.owl.library.exception.slot.LockedSlotException;
import net.melaircraft.owl.library.exception.slot.ResizeWouldTruncateSlotException;

public class ByteBufferDiskBundle implements DiskBundle {
    /** Sector size on DFS disk. */
    protected final int SECTOR_SIZE = 256;
    /** Sectors per track on disk. */
    protected final int SECTORS_PER_TRACK = 10;
    /** NUmber of tracks on a side of a disk. */
    protected final int TRACKS_PER_DISK = 80;

    /** Total number of bytes for a disk. */
    protected final int DISK_SIZE = SECTOR_SIZE * SECTORS_PER_TRACK * TRACKS_PER_DISK;

    /** Number of sectors at start of MMB for header and catalogue. */
    protected final int MMB_SECTOR_COUNT = 2;
    /** Offset until first disk. */
    protected final int INITIAL_OFFSET = SECTOR_SIZE * MMB_SECTOR_COUNT;

    @Override
    public void setBootSlot(int drive, int slot) throws InvalidDriveException, InvalidSlotException {

    }

    @Override
    public int getBootSlot(int drive) throws InvalidDriveException {
        return 0;
    }

    @Override
    public boolean isOccupied(int slot) throws InvalidSlotException {
        return false;
    }

    @Override
    public void activate(int slot) throws InvalidSlotException {

    }

    @Override
    public void deactivate(int slot, boolean wipe) throws InvalidSlotException, LockedSlotException {

    }

    @Override
    public Disk extract(int slot) throws InvalidSlotException, InactivateSlotException {
        return null;
    }

    @Override
    public void insert(int slot, Disk disk) throws InvalidSlotException, LockedSlotException {

    }

    @Override
    public boolean isLocked(int slot) throws InvalidSlotException, InactivateSlotException {
        return false;
    }

    @Override
    public void lock(int slot) throws InvalidSlotException, InactivateSlotException {

    }

    @Override
    public void unlock(int slot) throws InvalidSlotException, InactivateSlotException {

    }

    @Override
    public String getName(int slot) throws InvalidSlotException, InactivateSlotException {
        return null;
    }

    @Override
    public void rename(int slot, String name) throws InvalidSlotException {

    }

    @Override
    public int getStorageSize() {
        return 0;
    }

    @Override
    public void changeStorageSize(int slot) throws InvalidSlotException, ResizeWouldTruncateSlotException {

    }
}
