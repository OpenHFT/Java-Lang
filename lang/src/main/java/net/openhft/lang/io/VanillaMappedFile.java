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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/*
 * Merge memory mapped files:
 * - net.openhft.lang.io.MappedFile
 * - net.openhft.lang.io.MappedStore
 * - net.openhft.chronicle.VanillaFile
 */
public class VanillaMappedFile implements VanillaMappedResource {

    private final File path;
    private final FileChannel channel;
    private final VanillaMappedMode mode;
    private final long size;
    private final FileLifecycleListener fileLifecycleListener;

    public VanillaMappedFile(final File path, VanillaMappedMode mode) throws IOException {
        this(path, mode, -1, FileLifecycleListener.FileLifecycleListeners.IGNORE);
    }

    public VanillaMappedFile(final File path, VanillaMappedMode mode, long size,
                             FileLifecycleListener fileLifecycleListener) throws IOException {
        this.path = path;
        this.mode = mode;
        this.size = size;
        this.channel = fileChannel(path, mode, this.size, fileLifecycleListener);
        this.fileLifecycleListener = fileLifecycleListener;
    }

    public VanillaMappedBytes bytes(long address, long size) throws IOException {
        return new VanillaMappedBytes(map(address,size), -1, null);
    }

    public VanillaMappedBytes bytes(long address, long size, long index) throws IOException {
        return new VanillaMappedBytes(map(address,size), index, null);
    }

    @Override
    public String path() {
        return this.path.getAbsolutePath();
    }

    @Override
    public long size() {
        try {
            return this.channel.size();
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if(this.channel.isOpen()) {
            this.channel.close();
        }
    }

    // *************************************************************************
    // Helpers
    // *************************************************************************

    private synchronized MappedByteBuffer map(long address, long size) throws IOException {
        long start = System.nanoTime();
        MappedByteBuffer buffer = this.channel.map(this.mode.mapValue(),address,size);
        buffer.order(ByteOrder.nativeOrder());
        fileLifecycleListener.onFileGrowth(path, System.nanoTime() - start);
        return buffer;
    }

    private static FileChannel fileChannel(final File path, VanillaMappedMode mapMode, long size, FileLifecycleListener fileLifecycleListener) throws IOException {
        long start = System.nanoTime();
        FileChannel fileChannel = null;
        try {
            final RandomAccessFile raf = new RandomAccessFile(path, mapMode.stringValue());
            if (size > 0 && raf.length() != size) {
                if (mapMode.mapValue() != FileChannel.MapMode.READ_WRITE) {
                    throw new IOException("Cannot resize file to " + size + " as mode is not READ_WRITE");
                }

                raf.setLength(size);
            }

            fileChannel = raf.getChannel();
            //fileChannel.force(true);
        } catch (Exception e) {
            throw wrap(e);
        }

        fileLifecycleListener.onFileGrowth(path, System.nanoTime() - start);
        return fileChannel;
    }

    private static IOException wrap(Throwable throwable) {
        if(throwable instanceof InvocationTargetException) {
            throwable = throwable.getCause();

        } else if(throwable instanceof IOException) {
            return (IOException)throwable;
        }

        return new IOException(throwable);
    }

    public static VanillaMappedFile readWrite(final File path) throws IOException {
        return new VanillaMappedFile(path,VanillaMappedMode.RW);
    }

    public static VanillaMappedFile readWrite(final File path, long size) throws IOException {
        return new VanillaMappedFile(path, VanillaMappedMode.RW, size,
                FileLifecycleListener.FileLifecycleListeners.IGNORE);
    }

    public static VanillaMappedFile readOnly(final File path) throws IOException {
        return new VanillaMappedFile(path,VanillaMappedMode.RO);
    }

    public static VanillaMappedFile readOnly(final File path, long size) throws IOException {
        return new VanillaMappedFile(path, VanillaMappedMode.RO, size,
                FileLifecycleListener.FileLifecycleListeners.IGNORE);
    }

    public static VanillaMappedBytes readWriteBytes(final File path, long size) throws IOException {
        return readWriteBytes(path, size, -1);
    }

    public static VanillaMappedBytes readWriteBytes(final File path, long size, long index) throws IOException {
        return readWriteBytes(path, size, index, FileLifecycleListener.FileLifecycleListeners.IGNORE);
    }

    public static VanillaMappedBytes readWriteBytes(final File path, long size, long index, FileLifecycleListener fileLifecycleListener) throws IOException {
        VanillaMappedFile vmf = new VanillaMappedFile(path, VanillaMappedMode.RW, -1, fileLifecycleListener);
        return new VanillaMappedBytes(vmf.map(0,size), index, vmf.channel);
    }
}
