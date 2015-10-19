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

import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class VanillaMappedBytes extends NativeBytes {
    private final File path;
    private final MappedByteBuffer buffer;
    private final FileChannel channel;
    private final FileLifecycleListener fileLifecycleListener;
    private final long index;
    private boolean unmapped;

    public VanillaMappedBytes(final File path, final MappedByteBuffer buffer) {
        this(path, buffer, -1, null, FileLifecycleListener.FileLifecycleListeners.IGNORE);
    }

    public VanillaMappedBytes(final File path, final MappedByteBuffer buffer, FileLifecycleListener fileLifecycleListener) {
        this(path, buffer, -1, null, fileLifecycleListener);
    }

    public VanillaMappedBytes(final File path, final MappedByteBuffer buffer, long index) {
        this(path, buffer, index, null, FileLifecycleListener.FileLifecycleListeners.IGNORE);
    }

    public VanillaMappedBytes(final File path, final MappedByteBuffer buffer, long index, FileLifecycleListener fileLifecycleListener) {
        this(path, buffer, index, null, fileLifecycleListener);
    }

    protected VanillaMappedBytes(final File path, final MappedByteBuffer buffer, long index, final FileChannel channel) {
        this(path, buffer, index, channel, FileLifecycleListener.FileLifecycleListeners.IGNORE);
    }

    protected VanillaMappedBytes(
            final File path,
            final MappedByteBuffer buffer,
            long index,
            final FileChannel channel,
            final FileLifecycleListener fileLifecycleListener) {

        super(
            buffer.capacity() == 0 ? NO_PAGE : ((DirectBuffer) buffer).address(),
            buffer.capacity() == 0 ? NO_PAGE : ((DirectBuffer) buffer).address() + buffer.capacity()
        );

        this.buffer = buffer;
        this.path = path;
        this.channel = channel;
        this.unmapped = false;
        this.index = index;
        this.fileLifecycleListener = fileLifecycleListener;
    }

    public long index() {
        return this.index;
    }

    public synchronized boolean unmapped() {
        return this.unmapped;
    }

    @Override
    public boolean release() {
        if(!unmapped()) {
           return super.release();
        }

        return false;
    }

    @Override
    protected synchronized void cleanup() {
        if(!this.unmapped) {
            Cleaner cl = ((DirectBuffer)this.buffer).cleaner();
            if (cl != null) {
                long start = System.nanoTime();
                cl.clean();

                fileLifecycleListener.onEvent(
                    FileLifecycleListener.EventType.UNMAP,
                    this.path,
                    System.nanoTime() - start
                );

            }

            try {
                if (this.channel != null && this.channel.isOpen()) {
                    this.channel.close();
                }
            } catch(IOException e) {
                throw new AssertionError(e);
            }

            this.unmapped = true;
        }

        super.cleanup();
    }

    public void force() {
        long start = System.nanoTime();
        this.buffer.force();

        fileLifecycleListener.onEvent(
            FileLifecycleListener.EventType.SYNC,
            this.path,
            System.nanoTime() - start
        );
    }

    @Override
    public ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse) {
        return sliceAsByteBuffer(toReuse, buffer);
    }
}
