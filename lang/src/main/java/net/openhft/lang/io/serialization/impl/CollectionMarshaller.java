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
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.model.constraints.Nullable;

import java.util.Collection;

abstract class CollectionMarshaller<E, C extends Collection<E>> {

    public static final int NULL_LENGTH = -1;
    final BytesMarshaller<E> eBytesMarshaller;

    protected CollectionMarshaller(BytesMarshaller<E> eBytesMarshaller) {
        this.eBytesMarshaller = eBytesMarshaller;
    }

    public void write(Bytes bytes, C c) {
        if (c == null) {
            bytes.writeStopBit(NULL_LENGTH);
            return;
        }
        bytes.writeStopBit(c.size());
        for (E e : c) {
            eBytesMarshaller.write(bytes, e);
        }
    }

    public C read(Bytes bytes) {
        return read(bytes, null);
    }

    abstract C newCollection();

    public C read(Bytes bytes, @Nullable C c) {
        long length = bytes.readStopBit();

        if (length == 0 && c != null) {
            c.clear();
            return c;
        }

        if (length < NULL_LENGTH || length > Integer.MAX_VALUE)
            throw new IllegalStateException("Invalid length: " + length);

        if (length == NULL_LENGTH)
            return null;

        if (c == null)
            c = newCollection();

        return readCollection(bytes, c, (int) length);
    }

    abstract C readCollection(Bytes bytes, C c, int length);
}
