package net.melaircraft.owl.library;

import net.melaircraft.owl.library.exception.drive.InvalidDriveException;
import net.melaircraft.owl.library.exception.slot.InactivateSlotException;
import net.melaircraft.owl.library.exception.slot.InvalidSlotException;
import net.melaircraft.owl.library.exception.slot.LockedSlotException;
import net.melaircraft.owl.library.exception.slot.NoStorageSlotException;
import net.melaircraft.owl.library.exception.slot.ResizeWouldTruncateSlotException;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ByteBufferDiskBundleTest {
    @Test
    public void testConstructingEmptyBundleIsCorrectSize() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle();

        assertEquals(0, byteBufferDiskBundle.getStorageSize());
        assertEquals(DiskBundle.INITIAL_OFFSET, byteBufferDiskBundle.getByteBuffer().limit());
    }

    @Test
    public void testConstructingEmptyBundleAllSlotsAreUnused() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle();
        ByteBuffer byteBuffer = byteBufferDiskBundle.getByteBuffer();

        for (int slot = 0; slot < 511; slot++) {
            int headerValue = byteBuffer.get((16 * (slot + 1)) + 15);

            assertEquals("Slot " + slot + " is not initialised.", DiskBundle.HEADER_FLAG_DISK_UNFORMATTED, headerValue);
            assertFalse("Slot " + slot + " is not unused.", byteBufferDiskBundle.isOccupied(slot));
        }
    }

    @Test
    public void testConstructingEmptyBundleBootSlotsAreZero() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle();
        ByteBuffer byteBuffer = byteBufferDiskBundle.getByteBuffer();

        for (int pos = 0; pos < 8; pos++) {
            assertEquals(0, byteBuffer.get(pos));
        }

        for (int drive = 0; drive < 4; drive++) {
            assertEquals("Drive " + drive + " is not set to slot 0.", 0, byteBufferDiskBundle.getBootSlot(drive));
        }
    }

    @Test(expected = InvalidDriveException.class)
    public void testGetBootSlotBelowDriveRange() {
        new ByteBufferDiskBundle().getBootSlot(-1);
    }

    @Test(expected = InvalidDriveException.class)
    public void testGetBootSlotAboveDriveRange() {
        new ByteBufferDiskBundle().getBootSlot(4);
    }

    @Test(expected = InvalidDriveException.class)
    public void testSetBootSlotBelowDriveRange() {
        new ByteBufferDiskBundle().setBootSlot(-1, 0);
    }

    @Test(expected = InvalidDriveException.class)
    public void testSetBootSlotAboveDriveRange() {
        new ByteBufferDiskBundle().setBootSlot(4, 0);
    }

    @Test(expected = InvalidSlotException.class)
    public void testSetBootSlotBelowSlotRange() {
        new ByteBufferDiskBundle().setBootSlot(0, -1);
    }

    @Test(expected = InvalidSlotException.class)
    public void testSetBootSlotAboveSlotRange() {
        new ByteBufferDiskBundle().setBootSlot(0, 511);
    }

    @Test(expected = InvalidSlotException.class)
    public void testIsOccupiedBelowSlotRange() {
        new ByteBufferDiskBundle().isOccupied(-1);
    }

    @Test(expected = InvalidSlotException.class)
    public void testIsOccupiedAboveSlotRange() {
        new ByteBufferDiskBundle().isOccupied(511);
    }

    @Test
    public void testSetBootSlot() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle();

        int drive = 2;
        int slot = 500;

        byteBufferDiskBundle.setBootSlot(drive, slot);

        assertEquals(slot, byteBufferDiskBundle.getBootSlot(drive));
    }

    @Test
    public void testGetStorageSizeZero() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(0);
        assertEquals(0, byteBufferDiskBundle.getStorageSize());
    }

    @Test
    public void testGetStorageSizeAll() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(511);
        assertEquals(511, byteBufferDiskBundle.getStorageSize());
    }

    @Test(expected = InvalidSlotException.class)
    public void testConstructingOversizedBundle() {
        new ByteBufferDiskBundle(512);
    }

    @Test(expected = InvalidSlotException.class)
    public void testActivatingBelowSlotRange() {
        new ByteBufferDiskBundle().activate(-1);
    }

    @Test(expected = InvalidSlotException.class)
    public void testActivatingAboveSlotRange() {
        new ByteBufferDiskBundle().activate(511);
    }

    @Test
    public void testActivatingValidSlot() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);
        ByteBuffer byteBuffer = byteBufferDiskBundle.getByteBuffer();

        int slot = 0;

        byteBufferDiskBundle.activate(slot);

        int headerValue = byteBuffer.get((16 * (slot + 1)) + 15);
        assertEquals("Slot " + slot + " is not set to activated.", 0, headerValue);

        assertTrue(byteBufferDiskBundle.isOccupied(slot));
    }

    @Test(expected = NoStorageSlotException.class)
    public void testActivatingValidSlotWithNoStorage() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);

        int slot = 1;

        byteBufferDiskBundle.activate(slot);
    }

    @Test(expected = InvalidSlotException.class)
    public void testIsLockedBelowSlotRange() {
        new ByteBufferDiskBundle().isLocked(-1);
    }

    @Test(expected = InvalidSlotException.class)
    public void testIsLockedAboveSlotRange() {
        new ByteBufferDiskBundle().isLocked(511);
    }

    @Test(expected = InactivateSlotException.class)
    public void testIsLockedInactivateSlot() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);

        int slot = 1;

        byteBufferDiskBundle.isLocked(slot);
    }

    @Test
    public void testIsLockedActivatedUnlocked() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);
        ByteBuffer byteBuffer = byteBufferDiskBundle.getByteBuffer();

        int slot = 0;

        byteBufferDiskBundle.activate(slot);
        byteBufferDiskBundle.unlock(slot);

        int headerValue = byteBuffer.get((16 * (slot + 1)) + 15);
        assertEquals("Slot " + slot + " is not set to writable.", DiskBundle.HEADER_FLAG_DISK_WRITEABLE, headerValue);

        assertFalse(byteBufferDiskBundle.isLocked(slot));
    }

    @Test
    public void testIsLockedActivatedLocked() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);
        ByteBuffer byteBuffer = byteBufferDiskBundle.getByteBuffer();

        int slot = 0;

        byteBufferDiskBundle.activate(slot);

        int headerValue = byteBuffer.get((16 * (slot + 1)) + 15);
        assertEquals("Slot " + slot + " is not set to protected.", 0, headerValue);

        assertTrue(byteBufferDiskBundle.isLocked(slot));
    }

    @Test
    public void testIsLockedActivatedUnlockedThenLocked() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);

        int slot = 0;

        byteBufferDiskBundle.activate(slot);
        byteBufferDiskBundle.unlock(slot);
        byteBufferDiskBundle.lock(slot);

        assertTrue(byteBufferDiskBundle.isLocked(slot));
    }

    @Test(expected = InvalidSlotException.class)
    public void testLockBelowSlotRange() {
        new ByteBufferDiskBundle().lock(-1);
    }

    @Test(expected = InvalidSlotException.class)
    public void testLockAboveSlotRange() {
        new ByteBufferDiskBundle().lock(511);
    }

    @Test(expected = InactivateSlotException.class)
    public void testLockInactivateSlot() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);

        int slot = 1;

        byteBufferDiskBundle.lock(slot);
    }

    @Test(expected = InvalidSlotException.class)
    public void testUnlockBelowSlotRange() {
        new ByteBufferDiskBundle().unlock(-1);
    }

    @Test(expected = InvalidSlotException.class)
    public void testUnlockAboveSlotRange() {
        new ByteBufferDiskBundle().unlock(511);
    }

    @Test(expected = InactivateSlotException.class)
    public void testUnlockInactivateSlot() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);

        int slot = 1;

        byteBufferDiskBundle.unlock(slot);
    }

    @Test(expected = InvalidSlotException.class)
    public void testDeactivateBelowSlotRange() {
        new ByteBufferDiskBundle().deactivate(-1, false);
    }

    @Test(expected = InvalidSlotException.class)
    public void testDeactivateAboveSlotRange() {
        new ByteBufferDiskBundle().deactivate(511, false);
    }

    @Test(expected = LockedSlotException.class)
    public void testDeactivateLockedSlot() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);

        byteBufferDiskBundle.activate(0);
        byteBufferDiskBundle.deactivate(0, false);
    }

    @Test
    public void testDeactivateValid() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);
        ByteBuffer byteBuffer = byteBufferDiskBundle.getByteBuffer();

        int slot = 0;

        byteBufferDiskBundle.activate(slot);
        byteBufferDiskBundle.unlock(slot);
        byteBufferDiskBundle.deactivate(slot, false);

        int headerValue = byteBuffer.get((16 * (slot + 1)) + 15);
        assertEquals("Slot " + slot + " is not set to unformatted.", DiskBundle.HEADER_FLAG_DISK_UNFORMATTED, headerValue);
    }

    @Test
    public void testDeactivateValidWithWipe() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(2);
        ByteBuffer byteBuffer = byteBufferDiskBundle.getByteBuffer();

        int slot = 0;

        byteBufferDiskBundle.activate(slot);
        byteBufferDiskBundle.unlock(slot);

        for (int i = -1; i <= DiskBundle.DISK_SIZE; i++) {
            byteBuffer.put(DiskBundle.INITIAL_OFFSET + i, (byte) 0xff);
        }

        byteBufferDiskBundle.deactivate(slot, true);

        for (int i = 0; i < DiskBundle.DISK_SIZE; i++) {
            assertEquals("Disk data position " + i + " was not wiped!", 0, byteBuffer.get(DiskBundle.INITIAL_OFFSET + i));
        }

        assertEquals((byte) 0xff, byteBuffer.get(DiskBundle.INITIAL_OFFSET - 1));
        assertEquals((byte) 0xff, byteBuffer.get(DiskBundle.INITIAL_OFFSET + DiskBundle.DISK_SIZE));
    }

    @Test(expected = InvalidSlotException.class)
    public void testSetNameBelowSlotRange() {
        new ByteBufferDiskBundle().setName(-1, "");
    }

    @Test(expected = InvalidSlotException.class)
    public void testSetNameAboveSlotRange() {
        new ByteBufferDiskBundle().setName(511, "");
    }

    @Test
    public void testSetNameNormalName() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle();

        byteBufferDiskBundle.setName(0, "TEST");
        assertEquals("TEST", byteBufferDiskBundle.getName(0));
    }

    @Test
    public void testSetNameWithInvalidName() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle();

        byteBufferDiskBundle.setName(0, "TEST FILE");
        assertEquals("TEST", byteBufferDiskBundle.getName(0));
    }

    @Test(expected = InvalidSlotException.class)
    public void testGetNameBelowSlotRange() {
        new ByteBufferDiskBundle().getName(-1);
    }

    @Test(expected = InvalidSlotException.class)
    public void testGetNameAboveSlotRange() {
        new ByteBufferDiskBundle().getName(511);
    }

    @Test
    public void testGetNameWithSpacePadding() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle();
        ByteBuffer byteBuffer = byteBufferDiskBundle.getByteBuffer();

        byteBuffer.put(16, (byte) 'A');
        byteBuffer.put(17, (byte) ' ');
        byteBuffer.put(18, (byte) 'B');

        assertEquals("A", byteBufferDiskBundle.getName(0));
    }

    @Test
    public void testGetNameWithSpaceTrailing() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle();
        ByteBuffer byteBuffer = byteBufferDiskBundle.getByteBuffer();

        byteBuffer.put(16, (byte) 'A');
        byteBuffer.put(17, (byte) 'B');
        byteBuffer.put(18, (byte) ' ');
        byteBuffer.put(19, (byte) ' ');

        assertEquals("AB", byteBufferDiskBundle.getName(0));
    }

    @Test
    public void testGetNameWithNull() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle();

        assertEquals("", byteBufferDiskBundle.getName(0));
    }

    @Test(expected = InvalidSlotException.class)
    public void testChangeStorageSizeBelowSlotRange() {
        new ByteBufferDiskBundle().changeStorageSize(-1);
    }

    @Test(expected = InvalidSlotException.class)
    public void testChangeStorageSizeAboveSlotRange() {
        new ByteBufferDiskBundle().changeStorageSize(512);
    }

    @Test(expected = ResizeWouldTruncateSlotException.class)
    public void testChangeStorageSizeWouldTruncate() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(2);
        byteBufferDiskBundle.activate(1);

        byteBufferDiskBundle.changeStorageSize(1);
    }

    @Test
    public void testChangeStorageSizeReduce() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(2);

        byteBufferDiskBundle.changeStorageSize(1);
        assertEquals(1, byteBufferDiskBundle.getStorageSize());
    }

    @Test
    public void testChangeStorageSizeIncrease() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(2);

        byteBufferDiskBundle.changeStorageSize(4);
        assertEquals(4, byteBufferDiskBundle.getStorageSize());
    }

    @Test
    public void testChangeStorageSizeSame() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(2);

        ByteBuffer original = byteBufferDiskBundle.getByteBuffer();
        byteBufferDiskBundle.changeStorageSize(2);
        ByteBuffer after = byteBufferDiskBundle.getByteBuffer();

        /* We are actually doing an identity comparison to ensure the byte buffer has not been replaced. */
        assertTrue(original == after);
    }

    @Test(expected = InvalidSlotException.class)
    public void testExtractBelowSlotRange() {
        new ByteBufferDiskBundle().extract(-1);
    }

    @Test(expected = InvalidSlotException.class)
    public void testExtractAboveSlotRange() {
        new ByteBufferDiskBundle().extract(512);
    }

    @Test(expected = InactivateSlotException.class)
    public void testExtractInactiveSlot() {
        new ByteBufferDiskBundle().extract(1);
    }

    @Test
    public void testExtract() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(3);
        ByteBuffer byteBuffer = byteBufferDiskBundle.getByteBuffer();

        byteBufferDiskBundle.activate(1);

        byteBuffer.put(DiskBundle.INITIAL_OFFSET + DiskBundle.DISK_SIZE, (byte) 0xff);
        byteBuffer.put(DiskBundle.INITIAL_OFFSET + DiskBundle.DISK_SIZE + DiskBundle.DISK_SIZE - 1, (byte) 0xff);

        Disk disk = byteBufferDiskBundle.extract(1);
        ByteBufferDisk byteBufferDisk = (ByteBufferDisk) disk;

        assertEquals((byte) 0xff, byteBufferDisk.getByteBuffer().get(0));
        assertEquals((byte) 0xff, byteBufferDisk.getByteBuffer().get(DiskBundle.DISK_SIZE - 1));
    }

    @Test(expected = InvalidSlotException.class)
    public void testInsertBelowSlotRange() {
        new ByteBufferDiskBundle().insert(-1, null);
    }

    @Test(expected = InvalidSlotException.class)
    public void testInsertAboveSlotRange() {
        new ByteBufferDiskBundle().insert(512, null);
    }

    @Test(expected = LockedSlotException.class)
    public void testInsertLockedSlot() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);

        byteBufferDiskBundle.activate(0);
        byteBufferDiskBundle.lock(0);

        byteBufferDiskBundle.insert(0, null);
    }

    @Test
    public void testInsert() {
        ByteBufferDiskBundle byteBufferDiskBundle = new ByteBufferDiskBundle(1);

        byteBufferDiskBundle.activate(0);
        byteBufferDiskBundle.unlock(0);

        ByteBufferDisk byteBufferDisk = new ByteBufferDisk();
        byteBufferDisk.getByteBuffer().put(0, (byte) 0xff);
        byteBufferDisk.getByteBuffer().put(DiskBundle.DISK_SIZE - 1, (byte) 0xff);

        byteBufferDiskBundle.insert(0, byteBufferDisk);

        assertEquals((byte) 0xff, byteBufferDiskBundle.getByteBuffer().get(DiskBundle.INITIAL_OFFSET));
        assertEquals((byte) 0xff, byteBufferDiskBundle.getByteBuffer().get(DiskBundle.INITIAL_OFFSET + DiskBundle.DISK_SIZE - 1));
    }

    private void dump(ByteBuffer byteBuffer) {
        for (int i = 0; i < byteBuffer.limit(); i++) {
            if (i % 16 == 0) {
                System.out.println();
            }

            System.out.print(Long.toHexString(byteBuffer.get(i) & 0xff) + " ");
        }

        System.out.println();
    }
}