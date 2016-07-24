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
