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

import java.nio.ByteBuffer;

public enum ByteBufferMarshaller implements CompactBytesMarshaller<ByteBuffer> {
    INSTANCE;

    @Override
    public byte code() {
        return BYTE_BUFFER_CODE;
    }

    @Override
    public void write(Bytes bytes, ByteBuffer byteBuffer) {
        int position = byteBuffer.position();
        bytes.writeStopBit(byteBuffer.remaining());
        bytes.write(byteBuffer);

        // reset the position back as we found it
        byteBuffer.position(position);
    }

    @Override
    public ByteBuffer read(Bytes bytes) {
        return read(bytes, null);
    }

    @Override
    public ByteBuffer read(Bytes bytes, @Nullable ByteBuffer byteBuffer) {
        long length = bytes.readStopBit();
        assert length <= Integer.MAX_VALUE;
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new IllegalStateException("Invalid length: " + length);
        }
        if (byteBuffer == null || byteBuffer.capacity() < length) {
            byteBuffer = newByteBuffer((int) length);

        } else {
            byteBuffer.position(0);
            byteBuffer.limit((int) length);
        }

        bytes.read(byteBuffer);
        byteBuffer.flip();
        return byteBuffer;
    }

    protected ByteBuffer newByteBuffer(int length) {
        return ByteBuffer.allocate(length);
    }
}
