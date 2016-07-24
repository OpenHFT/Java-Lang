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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VanillaMappedBlocks implements VanillaMappedResource {
    private final VanillaMappedFile mappedFile;
    private final List<VanillaMappedBytes> bytes;
    private final long blockSize;
    private final FileLifecycleListener fileLifecycleListener;

    private VanillaMappedBytes mb0;
    private VanillaMappedBytes mb1;

    public VanillaMappedBlocks(final File path, VanillaMappedMode mode, long blockSize, long overlapSize) throws IOException {
        this(path, mode, blockSize + overlapSize, null);
    }

    public VanillaMappedBlocks(final File path, VanillaMappedMode mode, long blockSize,
                               FileLifecycleListener fileLifecycleListener) throws IOException {
        this.fileLifecycleListener = fileLifecycleListener;
        this.mappedFile = new VanillaMappedFile(path, mode, -1, fileLifecycleListener);
        this.bytes = new ArrayList<VanillaMappedBytes>();
        this.blockSize = blockSize;
        this.mb0 = null;
        this.mb1 = null;
    }

    public static VanillaMappedBlocks readWrite(final File path, long size) throws IOException {
        return readWrite(path, size, FileLifecycleListener.FileLifecycleListeners.IGNORE);
    }

    public static VanillaMappedBlocks readOnly(final File path, long size) throws IOException {
        return readOnly(path, size, FileLifecycleListener.FileLifecycleListeners.IGNORE);
    }

    public static VanillaMappedBlocks readWrite(final File path, long size,
                                                FileLifecycleListener listener) throws IOException {
        return new VanillaMappedBlocks(path, VanillaMappedMode.RW, size, listener);
    }

    public static VanillaMappedBlocks readOnly(final File path, long size,
                                               FileLifecycleListener listener) throws IOException {
        return new VanillaMappedBlocks(path, VanillaMappedMode.RO, size, listener);
    }

    public synchronized VanillaMappedBytes acquire(long index) throws IOException {
        if (this.mb0 != null && this.mb0.index() == index) {
            this.mb0.reserve();
            return this.mb0;
        }

        if (this.mb1 != null && this.mb1.index() == index) {
            this.mb1.reserve();
            return this.mb1;
        }

        return acquire0(index);
    }

    protected VanillaMappedBytes acquire0(long index) throws IOException {

        if (this.mb1 != null) {
            this.mb1.release();
        }

        this.mb1 = this.mb0;
        this.mb0 = this.mappedFile.bytes(index * this.blockSize, this.blockSize, index);
        this.mb0.reserve();

        bytes.add(this.mb0);

        for (int i = bytes.size() - 1; i >= 0; i--) {
            if (bytes.get(i).unmapped()) {
                bytes.remove(i);
            }
        }

        return this.mb0;
    }

    @Override
    public String path() {
        return this.mappedFile.path();
    }

    @Override
    public synchronized long size() {
        return this.mappedFile.size();
    }

    @Override
    public synchronized void close() throws IOException {
        if (this.mb0 != null && !this.mb0.unmapped()) {
            this.mb0.release();
            this.mb0 = null;
        }

        if (this.mb1 != null && !this.mb1.unmapped()) {
            this.mb1.release();
            this.mb1 = null;
        }

        for (int i = bytes.size() - 1; i >= 0; i--) {
            bytes.get(i).cleanup();
        }

        this.bytes.clear();
        this.mappedFile.close();
    }
}
