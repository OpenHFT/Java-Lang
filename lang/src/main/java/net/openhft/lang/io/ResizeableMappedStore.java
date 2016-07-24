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

import net.openhft.lang.io.serialization.BytesMarshallableSerializer;
import net.openhft.lang.io.serialization.JDKZObjectSerializer;
import net.openhft.lang.io.serialization.ObjectSerializer;
import net.openhft.lang.io.serialization.impl.VanillaBytesMarshallerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

public final class ResizeableMappedStore extends AbstractMappedStore {
    public ResizeableMappedStore(File file, FileChannel.MapMode mode, long size)
            throws IOException {
        this(file, mode, size, BytesMarshallableSerializer.create(
                new VanillaBytesMarshallerFactory(), JDKZObjectSerializer.INSTANCE));
    }

    public ResizeableMappedStore(File file, FileChannel.MapMode mode, long size,
                                 ObjectSerializer objectSerializer) throws IOException {
        super(new MmapInfoHolder(), file, mode, 0L, size, objectSerializer);
    }

    /**
     * Resizes the underlying file and re-maps it. Warning! After this call
     * instances of {@link Bytes} obtained through {@link #bytes()} or
     * {@link #bytes(long, long)} are invalid and using them can lead to reading
     * arbitrary data or JVM crash! It is the callers responsibility to ensure
     * that these instances are not used after the method call.
     *
     * @param newSize
     * @throws IOException
     */
    public void resize(long newSize) throws IOException {
        validateSize(newSize);
        unmapAndSyncToDisk();
        resizeIfNeeded(0L, newSize);
        this.mmapInfoHolder.setSize(newSize);
        map(0L);
    }
}
