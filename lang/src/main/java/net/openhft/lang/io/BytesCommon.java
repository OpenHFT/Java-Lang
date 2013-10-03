/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.io;

import net.openhft.lang.io.serialization.BytesMarshallerFactory;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

/**
 * @author peter.lawrey
 */
public interface BytesCommon {
    /**
     * @return the offset read/written so far
     */
    long position();

    /**
     * @param position to skip to
     */
    void position(long position);

    /**
     * @return space available
     */
    long capacity();

    /**
     * @return space remaining in bytes
     */
    long remaining();

    /**
     * Mark the end of the message if writing and check we are at the end of the message if reading.
     *
     * @throws IndexOutOfBoundsException if too much data was written.
     */
    void finish() throws IndexOutOfBoundsException;

    /**
     * @return has finish been called.
     */
    boolean isFinished();

    /**
     * Start again, unfinished, position() == 0
     */
    void reset();

    /**
     * @return Byte order for reading binary
     */
    @NotNull
    ByteOrder byteOrder();

    /**
     * @return these Bytes as an InputStream
     */
    @NotNull
    InputStream inputStream();

    /**
     * @return these Bytes as an OutputStream
     */
    @NotNull
    OutputStream outputStream();

    /**
     * @return the factory for marshallers.
     */
    @NotNull
    BytesMarshallerFactory bytesMarshallerFactory();

    /**
     * @throws IndexOutOfBoundsException if the bounds of the Bytes has been exceeded.
     */
    void checkEndOfBuffer() throws IndexOutOfBoundsException;

    /**
     * Lock which uses 4 bytes.  It store the lower 24 bits of the Thread Id and the re-entrant count as 8 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 255 times
     * you will get an ISE
     *
     * @param offset of the start of the 4-byte lock
     * @return did it lock or not.
     */
    boolean tryLockInt(long offset);

    /**
     * Lock which uses 4 bytes.  It store the lower 24 bits of the Thread Id and the re-entrant count as 8 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 255 times
     * you will get an ISE
     *
     * @param offset of the start of the 4-byte lock
     * @param nanos  to try to lock for
     * @return did it lock or not.
     */
    boolean tryLockNanosInt(long offset, long nanos);

    /**
     * Lock which uses 4 bytes.  It store the lower 24 bits of the Thread Id and the re-entrant count as 8 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 255 times
     * you will get an ISE
     *
     * @param offset of the start of the 4-byte lock
     * @throws InterruptedException  if interrupted
     * @throws IllegalStateException if the thread tries to lock it 255 nested time (without an unlock)
     */
    void busyLockInt(long offset) throws InterruptedException, IllegalStateException;

    /**
     * Lock which uses 4 bytes.  Unlock this It store the lower 24 bits of the Thread Id and the re-entrant count as 8
     * bit.  This means if you create more than 16 million threads you can get a collision, and if you try to re-enter
     * 255 times you will get an ISE
     *
     * @param offset of the start of the 4-byte lock
     * @throws IllegalMonitorStateException if this thread doesn't hold the lock
     */
    void unlockInt(long offset) throws IllegalMonitorStateException;
}
