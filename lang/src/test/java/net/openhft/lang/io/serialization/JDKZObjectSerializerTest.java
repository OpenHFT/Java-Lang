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

package net.openhft.lang.io.serialization;

import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.DirectStore;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.DeflaterOutputStream;

import static org.junit.Assert.assertEquals;

public class JDKZObjectSerializerTest {

    @Test
    public void testReadSerializable() throws IOException, ClassNotFoundException {
        {
            DirectBytes bytes = DirectStore.allocate(1024).bytes();
            bytes.writeInt(0);
            ObjectOutputStream oos = new ObjectOutputStream(bytes.outputStream());
            oos.writeObject("hello");
            oos.close();
            bytes.writeUnsignedInt(0, bytes.position() - 4);

            bytes.flip();
            assertEquals("hello", JDKZObjectSerializer.INSTANCE.readSerializable(bytes, null, null));
            bytes.release();
        }
        {
            DirectBytes bytes = DirectStore.allocate(1024).bytes();
            bytes.writeInt(0);
            ObjectOutputStream oos = new ObjectOutputStream(new DeflaterOutputStream(bytes.outputStream()));
            oos.writeObject("hello world");
            oos.close();
            bytes.writeUnsignedInt(0, bytes.position() - 4);

            bytes.flip();
            assertEquals("hello world", JDKZObjectSerializer.INSTANCE.readSerializable(bytes, null, null));
            bytes.close();
        }
    }
}