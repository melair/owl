package net.melaircraft.owl.library;

import net.melaircraft.owl.library.exception.drive.InvalidDriveException;
import net.melaircraft.owl.library.exception.slot.InactivateSlotException;
import net.melaircraft.owl.library.exception.slot.InvalidSlotException;
import net.melaircraft.owl.library.exception.slot.LockedSlotException;
import net.melaircraft.owl.library.exception.slot.NoStorageSlotException;
import net.melaircraft.owl.library.exception.slot.ResizeWouldTruncateSlotException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

/**
 * Implementation of a disk bundle backed by a byte buffer.
 */
public class ByteBufferDiskBundle implements DiskBundle {
    /** Underlying ByteBuffer for storage. */
    private ByteBuffer byteBuffer;

    /**
     * Construct an empty, zero disk storage disk bundle.
     */
    public ByteBufferDiskBundle() {
        this(0);
    }

    /**
     * Construct a new byte buffer disk bundle with a specified number of disks allocated.
     *
     * @param initialSlots initial slots allocated
     */
    public ByteBufferDiskBundle(int initialSlots) {
        if (initialSlots < 0 || initialSlots > 511) {
            throw new InvalidSlotException(initialSlots);
        }

        byteBuffer = ByteBuffer.allocate(calculateStorageSize(initialSlots));

        for (int slot = 0; slot < 511; slot++) {
            setFlag(slot, HEADER_FLAG_DISK_UNFORMATTED);
        }
    }

    @Override
    public void setBootSlot(int drive, int slot) throws InvalidDriveException, InvalidSlotException {
        checkDrive(drive);
        checkSlot(slot);

        byte value = (byte) (slot & 0xff);
        byte hiValue = (byte) ((slot >> 8) & 0xff);

        byteBuffer.put(drive, value);
        byteBuffer.put(drive + 4, hiValue);
    }

    @Override
    public int getBootSlot(int drive) throws InvalidDriveException {
        checkDrive(drive);

        int value = byteBuffer.get(drive) & 0xff;
        int hiValue = (byteBuffer.get(drive + 4) & 0xff) << 8;

        return hiValue + value;
    }

    @Override
    public boolean isOccupied(int slot) throws InvalidSlotException {
        checkSlot(slot);

        byte value = byteBuffer.get(getSlotPos(slot) + HEADER_FLAG_OFFSET);
        return (value & HEADER_FLAG_DISK_UNFORMATTED) != HEADER_FLAG_DISK_UNFORMATTED;
    }

    @Override
    public void activate(int slot) throws InvalidSlotException {
        checkSlot(slot);

        if (slot >= getStorageSize()) {
            throw new NoStorageSlotException(slot);
        }

        unsetFlag(slot, HEADER_FLAG_DISK_UNFORMATTED);
    }

    @Override
    public void deactivate(int slot, boolean wipe) throws InvalidSlotException, LockedSlotException {
        checkSlot(slot);

        if (isOccupied(slot) && isLocked(slot)) {
            throw new LockedSlotException(slot);
        }

        unsetFlag(slot, HEADER_FLAG_DISK_WRITEABLE);
        setFlag(slot, HEADER_FLAG_DISK_UNFORMATTED);

        if (wipe) {
            for (int i = 0; i < DISK_SIZE; i++) {
                byteBuffer.put(INITIAL_OFFSET + (DISK_SIZE * slot) + i, (byte) 0);
            }
        }
    }

    @Override
    public Disk extract(int slot) throws InvalidSlotException, InactivateSlotException {
        checkSlot(slot);
        checkSlotOccupied(slot);

        if (slot >= getStorageSize()) {
            throw new NoStorageSlotException(slot);
        }

        byte[] diskRaw = new byte[DiskBundle.DISK_SIZE];

        for (int i = 0; i < DiskBundle.DISK_SIZE; i++) {
            diskRaw[i] = byteBuffer.get(INITIAL_OFFSET + (DISK_SIZE * slot) + i);
        }

        return new ByteBufferDisk(diskRaw);
    }

    @Override
    public void insert(int slot, Disk disk) throws InvalidSlotException, LockedSlotException {
        checkSlot(slot);

        if (isOccupied(slot) && isLocked(slot)) {
            throw new LockedSlotException(slot);
        }

        for (int i = 0; i < disk.getImage().length && i < DiskBundle.DISK_SIZE; i++) {
            byteBuffer.put(DiskBundle.INITIAL_OFFSET + (slot * DiskBundle.DISK_SIZE) + i, disk.getImage()[i]);
        }
    }

    @Override
    public boolean isLocked(int slot) throws InvalidSlotException, InactivateSlotException {
        checkSlot(slot);
        checkSlotOccupied(slot);

        byte value = byteBuffer.get(getSlotPos(slot) + HEADER_FLAG_OFFSET);
        return (value & HEADER_FLAG_DISK_WRITEABLE) != HEADER_FLAG_DISK_WRITEABLE;
    }

    @Override
    public void lock(int slot) throws InvalidSlotException, InactivateSlotException {
        checkSlot(slot);
        checkSlotOccupied(slot);

        unsetFlag(slot, HEADER_FLAG_DISK_WRITEABLE);
    }

    @Override
    public void unlock(int slot) throws InvalidSlotException, InactivateSlotException {
        checkSlot(slot);
        checkSlotOccupied(slot);

        setFlag(slot, HEADER_FLAG_DISK_WRITEABLE);
    }

    @Override
    public String getName(int slot) throws InvalidSlotException {
        checkSlot(slot);

        byte[] nameBytes = new byte[MAXIMUM_DISK_TITLE];

        byteBuffer.position(getSlotPos(slot));
        byteBuffer.get(nameBytes);

        int validLength = IntStream.range(0, nameBytes.length).filter(i -> nameBytes[i] == 0 || nameBytes[i] == 32).findFirst().orElse(nameBytes.length);

        return new String(nameBytes, 0, validLength);
    }

    @Override
    public void setName(int slot, String name) throws InvalidSlotException {
        checkSlot(slot);

        String[] spaceSplitName = name.split(" ", 2);
        byte[] nameBytes = spaceSplitName[0].getBytes(StandardCharsets.US_ASCII);

        for (int i = 0; i < nameBytes.length && i < MAXIMUM_DISK_TITLE; i++) {
            byteBuffer.put(getSlotPos(slot) + i, nameBytes[i]);
        }
    }

    @Override
    public int getStorageSize() {
        return (byteBuffer.limit() - INITIAL_OFFSET) / DISK_SIZE;
    }

    @Override
    public void changeStorageSize(int slots) throws InvalidSlotException, ResizeWouldTruncateSlotException {
        if (slots < 0 || slots > 511) {
            throw new InvalidSlotException(slots);
        }

        for (int i = Math.max(0, (slots - 1)); i < 511; i++) {
            if (isOccupied(slots)) {
                throw new ResizeWouldTruncateSlotException(i);
            }
        }

        int currentSlots = getStorageSize();

        if (currentSlots != slots) {
            int newSize = calculateStorageSize(slots);

            int originalLimit = byteBuffer.limit();

            if (currentSlots > slots) {
                byteBuffer.limit(newSize);
            }

            byteBuffer.position(0);

            ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
            newBuffer.put(byteBuffer);

            byteBuffer.limit(originalLimit);

            byteBuffer = newBuffer;
        }
    }

    /**
     * Calculate the byte size of a bundle with the slot count.
     *
     * @param slots number of allocated slots
     * @return byte could of a bundle which can contain the slot count
     */
    private int calculateStorageSize(int slots) {
        return INITIAL_OFFSET + (DISK_SIZE * slots);
    }

    /**
     * Get the byte position in the byte buffer of the catalogue entry.
     *
     * @param slot slot position
     * @return byte position in buffer
     */
    private int getSlotPos(int slot) {
        return 16 * (slot + 1);
    }

    /**
     * Check to see if a drive is valid, if not throw an exception.
     *
     * @param drive drive number to check
     * @throws InvalidDriveException if the drive provided is invalid
     */
    private void checkDrive(int drive) throws InvalidDriveException {
        if (drive < 0 || drive > 3) {
            throw new InvalidDriveException(drive);
        }
    }

    /**
     * Check to see if a slot is valid, if not throw an exception.
     *
     * @param slot slot number to check
     * @throws InvalidSlotException if the slot provided is invalid
     */
    private void checkSlot(int slot) throws InvalidSlotException {
        if (slot < 0 || slot > 510) {
            throw new InvalidSlotException(slot);
        }
    }

    /**
     * Check to see if a slot is occupied, if not throw an exception.
     *
     * @param slot slot number to check
     * @throws InactivateSlotException if the slot provided is inactive
     */
    private void checkSlotOccupied(int slot) throws InactivateSlotException {
        if (!isOccupied(slot)) {
            throw new InactivateSlotException(slot);
        }
    }

    /**
     * Set a flag in the bundle catalogue.
     *
     * @param slot slot to set on
     * @param flag flag to set
     */
    private void setFlag(int slot, byte flag) {
        int position = getSlotPos(slot) + HEADER_FLAG_OFFSET;

        byte value = byteBuffer.get(position);
        byte newValue = (byte) (value | flag);

        byteBuffer.put(position, newValue);
    }

    /**
     * Unset a flag in the bundle catalogue.
     *
     * @param slot slot to set on
     * @param flag flag to unset
     */
    private void unsetFlag(int slot, byte flag) {
        int position = getSlotPos(slot) + HEADER_FLAG_OFFSET;

        byte value = byteBuffer.get(position);
        byte newValue = (byte) (value & ~flag);

        byteBuffer.put(position, newValue);
    }

    /**
     * Get the byte buffer which backs this disk bundle, used to allow saving.
     *
     * @return byte buffer backing bundle
     */
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
}
