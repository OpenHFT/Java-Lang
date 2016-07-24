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

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static org.junit.Assert.assertEquals;

public final class ResizeableMappedStoreTest {
    @After
    public void tearDown() {
        System.gc();
    }

    @Test
    public void testResizableMappedStore() throws IOException {
        File file = MappedStoreTest.getStoreFile("resizable-mapped-store.tmp");

        final int smallSize = 1024, largeSize = 10 * smallSize;

        {
            ResizeableMappedStore ms = new ResizeableMappedStore(file, FileChannel.MapMode.READ_WRITE, smallSize);

            DirectBytes slice1 = ms.bytes();
            for (int i = 0; i < smallSize; ++i) {
               slice1.writeByte(42);
            }

            ms.resize(largeSize);

            DirectBytes slice2 = ms.bytes();
            slice2.skipBytes(smallSize);
            for (int i = smallSize; i < largeSize; ++i) {
               slice2.writeByte(24);
            }

            ms.close();
        }

        assertEquals(largeSize, file.length());

        {
            ResizeableMappedStore ms = new ResizeableMappedStore(file, FileChannel.MapMode.READ_WRITE, file.length());
            DirectBytes slice = ms.bytes();
            assertEquals(42, slice.readByte(smallSize - 1));
            assertEquals(24, slice.readByte(largeSize - 1));
            slice.release();

            ms.close();
        }
    }
}
