package net.melaircraft.owl.library;

import net.melaircraft.owl.library.exception.slot.InvalidSlotException;
import net.melaircraft.owl.library.exception.slot.ResizeWouldTruncateSlotException;

public class FileChannelDiskBundle extends ByteBufferDiskBundle {
    @Override
    public void changeStorageSize(int slot) throws InvalidSlotException, ResizeWouldTruncateSlotException {

    }
}
