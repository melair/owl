package net.melaircraft.owl.library;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class ByteBufferDiskTest {
    @Test
    public void testConstructionOfCorrectSize() {
        ByteBufferDisk disk = new ByteBufferDisk();

        assertEquals(DiskBundle.DISK_SIZE, disk.getByteBuffer().limit());
    }

    @Test
    public void testConstructionWithSourceMaterial() {
        byte[] bytes = new byte[DiskBundle.DISK_SIZE];

        bytes[0] = (byte) 0xff;
        bytes[DiskBundle.DISK_SIZE - 1] = (byte) 0xff;

        ByteBufferDisk disk = new ByteBufferDisk(bytes);

        ByteBuffer diskBuffer = disk.getByteBuffer();

        assertEquals((byte) 0xff, diskBuffer.get(0));
        assertEquals((byte) 0xff, diskBuffer.get(DiskBundle.DISK_SIZE - 1));
    }
}