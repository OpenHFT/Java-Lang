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

import net.openhft.lang.io.serialization.BytesMarshallableSerializer;
import net.openhft.lang.io.serialization.BytesMarshallerFactory;
import net.openhft.lang.io.serialization.JDKZObjectSerializer;
import net.openhft.lang.io.serialization.ObjectSerializer;
import net.openhft.lang.io.serialization.impl.VanillaBytesMarshallerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class MappedStore extends AbstractMappedStore {
    public MappedStore(File file, FileChannel.MapMode mode, long size) throws IOException {
        this(file, mode, size, new VanillaBytesMarshallerFactory());
    }

    @Deprecated
    public MappedStore(File file, FileChannel.MapMode mode, long size,
            BytesMarshallerFactory bytesMarshallerFactory) throws IOException {
        this(file, mode, size, BytesMarshallableSerializer.create(
                bytesMarshallerFactory, JDKZObjectSerializer.INSTANCE));
    }

    public MappedStore(File file, FileChannel.MapMode mode, long size,
            ObjectSerializer objectSerializer) throws IOException {
        this(file, mode, 0L, size, objectSerializer);
    }

    public MappedStore(File file, FileChannel.MapMode mode, long startInFile, long size,
            ObjectSerializer objectSerializer) throws IOException {
        super(new MmapInfoHolder(), file, mode, startInFile, size, objectSerializer);
        mmapInfoHolder.lock();
    }
}

