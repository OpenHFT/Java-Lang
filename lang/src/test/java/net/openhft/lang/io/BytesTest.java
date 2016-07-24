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

import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * Created by peter.lawrey on 06/08/2015.
 */
public class BytesTest {
    @Test
    public void testReadEnum() {
        Bytes b = DirectStore.allocate(128).bytes();
        b.writeEnum("Hello");
        b.writeEnum("World");
        b.flip();
        String x = b.readEnum(String.class);
        String y = b.readEnum(String.class);
        assertNotSame(x, y);
        b.position(0);
        String x2 = b.readEnum(String.class);
        String y2 = b.readEnum(String.class);
        assertNotSame(x2, y2);
        assertSame(x, x2);
        assertSame(y, y2);
    }
}
