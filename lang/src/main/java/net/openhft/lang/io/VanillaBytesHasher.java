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

import net.openhft.lang.Maths;

public enum VanillaBytesHasher implements BytesHasher {
    INSTANCE;
    private static final long LONG_LEVEL_PRIME_MULTIPLE = 0x9ddfea08eb382d69L;
    private static final short SHORT_LEVEL_PRIME_MULTIPLE = 0x404f;
    private static final byte BYTE_LEVEL_PRIME_MULTIPLE = 0x57;

    public long hash(Bytes bytes) {
        return hash(bytes, bytes.position(), bytes.limit());
    }

    public long hash(Bytes bytes, long offset, long limit) {
        return Maths.hash(limit - offset == 8 ? bytes.readLong(offset) : hash0(bytes, offset, limit));
    }

    private long hash0(Bytes bytes, long offset, long limit) {
        long h = 0;
        long i = offset;
        for (; i < limit - 7; i += 8)
            h = LONG_LEVEL_PRIME_MULTIPLE * h + bytes.readLong(i);
        for (; i < limit - 1; i += 2)
            h = SHORT_LEVEL_PRIME_MULTIPLE * h + bytes.readShort(i);
        if (i < limit)
            h = BYTE_LEVEL_PRIME_MULTIPLE * h + bytes.readByte(i);
        return h;
    }
}
