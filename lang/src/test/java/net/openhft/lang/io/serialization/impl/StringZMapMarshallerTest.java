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
import net.openhft.lang.io.DirectStore;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StringZMapMarshallerTest {
    static final Bytes b = DirectStore.allocate(1024).bytes();

    public static <K, V> Map<K, V> mapOf(K k, V v, Object... keysAndValues) {
        Map<K, V> ret = new LinkedHashMap<K, V>();
        ret.put(k, v);
        for (int i = 0; i < keysAndValues.length - 1; i += 2) {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];
            ret.put((K) key, (V) value);
        }
        return ret;
    }

    @Test
    public void testWriteRead() {
        testWriteRead(null);
        testWriteRead(Collections.<String, String>emptyMap());
        testWriteRead(mapOf("Hello", "World", "aye", "alpha", "bee", "beta", "zed", "zeta"));
    }

    private void testWriteRead(Map<String, String> map) {
        b.clear();
        StringZMapMarshaller.INSTANCE.write(b, map);
        b.writeInt(0x12345678);
        b.flip();
        Map<String, String> s2 = StringZMapMarshaller.INSTANCE.read(b);
        assertEquals(map, s2);
        assertEquals(0x12345678, b.readInt());
    }
}