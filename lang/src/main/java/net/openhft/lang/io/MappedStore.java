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

