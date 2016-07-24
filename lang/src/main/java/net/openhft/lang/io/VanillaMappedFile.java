/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.lang.io;

import net.openhft.lang.io.FileLifecycleListener.EventType;

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
    private final FileChannel fileChannel;
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
        this.fileChannel = fileChannel(path, mode, this.size, fileLifecycleListener);
        this.fileLifecycleListener = fileLifecycleListener;
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
        } catch (Exception e) {
            throw wrap(e);
        }

        fileLifecycleListener.onEvent(EventType.NEW, path, System.nanoTime() - start);
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

    // *************************************************************************
    // Helpers
    // *************************************************************************

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
        return new VanillaMappedBytes(path, vmf.map(0,size), index, vmf.fileChannel, fileLifecycleListener);
    }

    public VanillaMappedBytes bytes(long address, long size) throws IOException {
        return new VanillaMappedBytes(this.path, map(address, size), -1, null, this.fileLifecycleListener);
    }

    public VanillaMappedBytes bytes(long address, long size, long index) throws IOException {
        return new VanillaMappedBytes(this.path, map(address, size), index, null, this.fileLifecycleListener);
    }

    @Override
    public String path() {
        return this.path.getAbsolutePath();
    }

    @Override
    public long size() {
        try {
            return this.fileChannel.size();
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (this.fileChannel.isOpen()) {
            long start = System.nanoTime();
            this.fileChannel.close();
            this.fileLifecycleListener.onEvent(EventType.CLOSE, this.path, System.nanoTime() - start);
        }
    }

    private synchronized MappedByteBuffer map(long address, long size) throws IOException {
        long start = System.nanoTime();
        MappedByteBuffer buffer = this.fileChannel.map(this.mode.mapValue(), address, size);
        buffer.order(ByteOrder.nativeOrder());
        fileLifecycleListener.onEvent(EventType.MMAP, path, System.nanoTime() - start);
        return buffer;
    }
}
