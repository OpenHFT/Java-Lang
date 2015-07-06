/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.openhft.lang.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Test;

public final class ResizableMappedStoreTest {
    @After
    public void tearDown() {
        System.gc();
    }

    @Test
    public void testResizableMappedStore() throws IOException {
        File file = MappedStoreTest.getStoreFile("resizable-mapped-store.tmp");

        final int smallSize = 1024, largeSize = 10 * smallSize;

        {
            ResizableMappedStore ms = new ResizableMappedStore(file, FileChannel.MapMode.READ_WRITE, smallSize);

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
            ResizableMappedStore ms = new ResizableMappedStore(file, FileChannel.MapMode.READ_WRITE, file.length());
            DirectBytes slice = ms.bytes();
            assertEquals(42, slice.readByte(smallSize - 1));
            assertEquals(24, slice.readByte(largeSize - 1));
            slice.release();

            ms.close();
        }
    }
}
