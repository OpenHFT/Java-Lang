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

package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.model.constraints.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public enum ByteBufferZMarshaller implements CompactBytesMarshaller<ByteBuffer> {
    INSTANCE;

    @Override
    public byte code() {
        return BYTE_BUFFER_CODE;
    }

    @Override
    public void write(Bytes bytes, ByteBuffer byteBuffer) {
        bytes.writeStopBit(byteBuffer.remaining());
        long position = bytes.position();
        bytes.clear();
        bytes.position(position + 4);
        DataOutputStream dos = new DataOutputStream(new DeflaterOutputStream(bytes.outputStream()));
        try {
            while (byteBuffer.remaining() >= 8)
                dos.writeLong(byteBuffer.getLong());
            while (byteBuffer.remaining() > 0)
                dos.write(byteBuffer.get());
            dos.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        bytes.writeUnsignedInt(position, bytes.position() - position - 4);

        bytes.write(byteBuffer);
    }

    @Override
    public ByteBuffer read(Bytes bytes) {
        return read(bytes, null);
    }

    @Override
    public ByteBuffer read(Bytes bytes, @Nullable ByteBuffer byteBuffer) {
        long length = bytes.readStopBit();
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new IllegalStateException("Invalid length: " + length);
        }
        if (byteBuffer == null || byteBuffer.capacity() < length) {
            byteBuffer = newByteBuffer((int) length);

        } else {
            byteBuffer.clear();
        }
        byteBuffer.limit((int) length);

        long position = bytes.position();
        long end = position + length;

        long limit = bytes.limit();
        bytes.limit(end);

        DataInputStream dis = new DataInputStream(new InflaterInputStream(bytes.inputStream()));
        try {
            while (byteBuffer.remaining() >= 8)
                byteBuffer.putLong(dis.readLong());
            while (byteBuffer.remaining() >= 0)
                byteBuffer.put(dis.readByte());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                dis.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        
        bytes.position(end);
        bytes.limit(limit);

        byteBuffer.flip();
        return byteBuffer;
    }

    protected ByteBuffer newByteBuffer(int length) {
        return ByteBuffer.allocate(length);
    }
}
