/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.openhft.lang.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.openhft.lang.io.serialization.ObjectSerializer;
import net.openhft.lang.model.constraints.NotNull;
import sun.misc.Cleaner;
import sun.nio.ch.FileChannelImpl;

abstract class AbstractMappedStore implements BytesStore, Closeable {
    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int MAP_PV = 2;

    // retain to prevent GC.
    private final File file;
    private final RandomAccessFile raf;
    private final Cleaner cleaner;
    private final AtomicInteger refCount = new AtomicInteger(1);
    private final FileChannel.MapMode mode;
    protected final MmapInfoHolder mmapInfoHolder;
    private ObjectSerializer objectSerializer;

    AbstractMappedStore(MmapInfoHolder mmapInfoHolder, File file, FileChannel.MapMode mode, long size, ObjectSerializer objectSerializer) throws IOException {
        validateSize(size);
        this.file = file;
        this.mmapInfoHolder = mmapInfoHolder;
        this.mmapInfoHolder.setSize(size);
        this.objectSerializer = objectSerializer;
        this.mode = mode;

        try {
            this.raf = new RandomAccessFile(file, accesModeFor(mode));
            resizeIfNeeded(size);
            map();
            this.cleaner = Cleaner.create(this, new Unmapper(mmapInfoHolder, raf));
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    protected final void validateSize(long size) {
        if (size <= 0 || size > 128L << 40) {
            throw new IllegalArgumentException("invalid size: " + size);
        }
    }

    protected final void resizeIfNeeded(long newSize) throws IOException {
        if (raf.length() == newSize) {
            return;
        }
        if (file.getAbsolutePath().startsWith("/dev/")) {
            return;
        }
        if (mode != FileChannel.MapMode.READ_WRITE) {
            throw new IOException("Cannot resize file to " + newSize + " as mode is not READ_WRITE");
        }

        raf.setLength(newSize);
    }

    protected final void map() throws IOException {
        try {
            mmapInfoHolder.setAddress(map0(raf.getChannel(), imodeFor(mode), 0L, mmapInfoHolder.getSize()));
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    protected final void unmapAndSyncToDisk() throws IOException {
        unmap0(mmapInfoHolder.getAddress(), mmapInfoHolder.getSize());
        raf.getChannel().force(true);
    }

    private static long map0(FileChannel fileChannel, int imode, long start, long size) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method map0 = fileChannel.getClass().getDeclaredMethod("map0", int.class, long.class, long.class);
        map0.setAccessible(true);
        return (Long) map0.invoke(fileChannel, imode, start, size);
    }

    private static void unmap0(long address, long size) throws IOException {
        try {
            Method unmap0 = FileChannelImpl.class.getDeclaredMethod("unmap0", long.class, long.class);
            unmap0.setAccessible(true);
            unmap0.invoke(null, address, size);
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    private static IOException wrap(Throwable e) {
        if (e instanceof InvocationTargetException)
            e = e.getCause();
        if (e instanceof IOException)
            return (IOException) e;
        return new IOException(e);
    }

    private static String accesModeFor(FileChannel.MapMode mode) {
        return mode == FileChannel.MapMode.READ_WRITE ? "rw" : "r";
    }

    private static int imodeFor(FileChannel.MapMode mode) {
        int imode = -1;
        if (mode == FileChannel.MapMode.READ_ONLY)
            imode = MAP_RO;
        else if (mode == FileChannel.MapMode.READ_WRITE)
            imode = MAP_RW;
        else if (mode == FileChannel.MapMode.PRIVATE)
            imode = MAP_PV;
        assert (imode >= 0);
        return imode;
    }

    @Override
    public final ObjectSerializer objectSerializer() {
        return objectSerializer;
    }

    @Override
    public final long address() {
        return mmapInfoHolder.getAddress();
    }

    @Override
    public final long size() {
        return mmapInfoHolder.getSize();
    }

    @Override
    public final  void free() {
        cleaner.clean();
    }

    @Override
    public final  void close() {
        free();
    }

    @NotNull
    public final  DirectBytes bytes() {
        return new DirectBytes(this, refCount);
    }

    @NotNull
    public final  DirectBytes bytes(long offset, long length) {
        return new DirectBytes(this, refCount, offset, length);
    }

    public final  File file() {
        return file;
    }

    abstract static class MmapInfoHolder {
        abstract void setAddress(long address);

        abstract long getAddress();

        abstract void setSize(long size);

        abstract long getSize();
    }

    private static final class Unmapper implements Runnable {
        private final MmapInfoHolder mmapInfoHolder;
        private final RandomAccessFile raf;
        /*
         * This is not for synchronization (since calling this from multiple
         * threads through .free / .close is an user error!) but rather to make
         * sure that if an explicit cleanup was performed, the cleaner does not
         * retry cleaning up the resources.
         */
        private volatile boolean cleanedUp;

        Unmapper(MmapInfoHolder mmapInfo, RandomAccessFile raf) {
            this.mmapInfoHolder = mmapInfo;
            this.raf = raf;
        }

        public void run() {
            if (cleanedUp) {
                return;
            }
            cleanedUp = true;

            try {
                unmap0(mmapInfoHolder.getAddress(), mmapInfoHolder.getSize());
                raf.getChannel().force(true);
                // this also closes the underlying channel as per the documentation
                raf.close();
            } catch (IOException e) {
                UnmapperLoggerHolder.LOGGER.log(Level.SEVERE,
                    "An exception has occurred while cleaning up a MappedStore instance: " + e.getMessage(), e);
            }
        }
    }

    private static final class UnmapperLoggerHolder {
        private static final Logger LOGGER = Logger.getLogger(Unmapper.class.getName());
    }
}
