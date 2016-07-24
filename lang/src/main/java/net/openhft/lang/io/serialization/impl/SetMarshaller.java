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
import net.openhft.lang.io.serialization.CompactBytesMarshaller;

import java.util.LinkedHashSet;
import java.util.Set;

public class SetMarshaller<E> extends CollectionMarshaller<E, Set<E>> implements CompactBytesMarshaller<Set<E>> {
    SetMarshaller(BytesMarshaller<E> eBytesMarshaller) {
        super(eBytesMarshaller);
    }

    public static <E> BytesMarshaller<Set<E>> of(BytesMarshaller<E> eBytesMarshaller) {
        return new SetMarshaller<E>(eBytesMarshaller);
    }

    @Override
    public byte code() {
        return SET_CODE;
    }

    @Override
    Set<E> newCollection() {
        return new LinkedHashSet<E>();
    }

    @Override
    Set<E> readCollection(Bytes bytes, Set<E> es, int length) {
        es.clear();

        for (int i = 0; i < length; i++) {
            es.add(eBytesMarshaller.read(bytes));
        }

        return es;
    }
}
