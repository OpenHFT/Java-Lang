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
import java.nio.channels.FileChannel;

import net.openhft.lang.io.serialization.BytesMarshallableSerializer;
import net.openhft.lang.io.serialization.JDKZObjectSerializer;
import net.openhft.lang.io.serialization.ObjectSerializer;
import net.openhft.lang.io.serialization.impl.VanillaBytesMarshallerFactory;

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
