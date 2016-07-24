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

import java.nio.charset.Charset;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SnappyStringMarshallerTest {

    @Test
    public void testWriteRead() {
        Bytes b = DirectStore.allocate(64 * 1024).bytes();
        testWriteRead(b, "");
        testWriteRead(b, null);
        testWriteRead(b, "Hello World");
        testWriteRead(b, new String(new char[1000000]));
        byte[] bytes = new byte[64000];
        Random random = new Random();
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) ('A' + random.nextInt(26));
        testWriteRead(b, new String(bytes, Charset.forName("ISO-8859-1")));
    }

    private void testWriteRead(Bytes b, String s) {
        b.clear();
        SnappyStringMarshaller.INSTANCE.write(b, s);
        b.writeInt(0x12345678);
        b.flip();
        String s2 = SnappyStringMarshaller.INSTANCE.read(b);
        assertEquals(0x12345678, b.readInt());
        assertEquals(s, s2);
    }
}