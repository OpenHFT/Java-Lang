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

import net.openhft.lang.io.serialization.BytesMarshallerFactory;
import net.openhft.lang.io.serialization.ObjectSerializer;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author peter.lawrey
 */
public class NativeBytes extends AbstractBytes {
    /**
     * *** Access the Unsafe class *****
     */
    @NotNull
    @SuppressWarnings("ALL")
    public static final Unsafe UNSAFE;
    protected static final long NO_PAGE;
    static final int BYTES_OFFSET;
    static final int CHARS_OFFSET;

    static {
        try {
            @SuppressWarnings("ALL")
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
            BYTES_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
            CHARS_OFFSET = UNSAFE.arrayBaseOffset(char[].class);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        NO_PAGE = UNSAFE.allocateMemory(UNSAFE.pageSize());
    }

    protected long startAddr;
    protected long positionAddr;
    protected long limitAddr;
    protected long capacityAddr;

    public NativeBytes(long startAddr, long capacityAddr) {
        super();
        setStartPositionAddress(startAddr);
        if (startAddr > capacityAddr)
            throw new IllegalArgumentException("Missorted capacity address");
        this.limitAddr =
                this.capacityAddr = capacityAddr;
        positionChecks(positionAddr);
    }

    public void setStartPositionAddress(long startAddr) {
        if ((startAddr & ~0x3fff) == 0)
            throw new AssertionError("Invalid address " + Long.toHexString(startAddr));
        this.positionAddr =
                this.startAddr = startAddr;
    }

    public static NativeBytes wrap(long address, long capacity) {
        return new NativeBytes(address, address + capacity);
    }

    /**
     * @deprecated Use {@link #NativeBytes(ObjectSerializer, long, long, AtomicInteger)} instead
     */
    @Deprecated
    public NativeBytes(BytesMarshallerFactory bytesMarshallerFactory,
                       long startAddr, long capacityAddr, AtomicInteger refCount) {
        super(bytesMarshallerFactory, refCount);

        setStartPositionAddress(startAddr);
        this.limitAddr =
                this.capacityAddr = capacityAddr;
        positionChecks(positionAddr);
    }

    public NativeBytes(ObjectSerializer objectSerializer,
                       long startAddr, long capacityAddr, AtomicInteger refCount) {
        super(objectSerializer, refCount);

        setStartPositionAddress(startAddr);
        this.limitAddr =
                this.capacityAddr = capacityAddr;
        positionChecks(positionAddr);
    }

    public NativeBytes(NativeBytes bytes) {
        super(bytes.objectSerializer(), new AtomicInteger(1));
        setStartPositionAddress(bytes.startAddr);

        this.positionAddr = bytes.positionAddr;
        this.limitAddr = bytes.limitAddr;
        this.capacityAddr = bytes.capacityAddr;
        positionChecks(positionAddr);
    }

    public static long longHash(byte[] bytes, int off, int len) {
        long hash = 0;
        int pos = 0;
        for (; pos < len - 7; pos += 8)
            hash = hash * 10191 + UNSAFE.getLong(bytes, (long) BYTES_OFFSET + off + pos);
        for (; pos < len; pos++)
            hash = hash * 57 + bytes[off + pos];
        return hash;
    }

    // optimised to reduce overhead.
    @Override
    public int readUTF0(final long offset, @NotNull Appendable appendable, int utflen) throws IOException {
        int count = 0;
        int bytesRead = 0;
        long address = startAddr + offset;
        if (address > limitAddr)
            throw new BufferUnderflowException();

        while (count < utflen) {
            int c = UNSAFE.getByte(address++) & 0xFF;
            bytesRead++;
            if (c >= 128) {
                bytesRead--;
                bytesRead += readUTF2(this, offset + bytesRead, appendable, utflen, count);
                break;
            }
            count++;
            appendable.append((char) c);
        }

        return bytesRead;
    }

    @Override
    public NativeBytes slice() {
        return new NativeBytes(objectSerializer(), positionAddr, limitAddr, refCount);
    }

    @Override
    public NativeBytes slice(long offset, long length) {
        long sliceStart = positionAddr + offset;
        assert sliceStart >= startAddr && sliceStart < capacityAddr;
        long sliceEnd = sliceStart + length;
        assert sliceEnd > sliceStart && sliceEnd <= capacityAddr;
        return new NativeBytes(objectSerializer(), sliceStart, sliceEnd, refCount);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        long subStart = positionAddr + start;
        if (subStart < positionAddr || subStart > limitAddr)
            throw new IndexOutOfBoundsException();
        long subEnd = positionAddr + end;
        if (subEnd < subStart || subEnd > limitAddr)
            throw new IndexOutOfBoundsException();
        if (start == end)
            return "";
        return new NativeBytes(objectSerializer(), subStart, subEnd, refCount);
    }

    @Override
    public NativeBytes bytes() {
        return new NativeBytes(objectSerializer(), startAddr, capacityAddr, refCount);
    }

    @Override
    public NativeBytes bytes(long offset, long length) {
        long sliceStart = startAddr + offset;
        assert sliceStart >= startAddr && sliceStart < capacityAddr;
        long sliceEnd = sliceStart + length;
        assert sliceEnd > sliceStart && sliceEnd <= capacityAddr;
        return new NativeBytes(objectSerializer(), sliceStart, sliceEnd, refCount);
    }

    @Override
    public long address() {
        return startAddr;
    }

    @Override
    public Bytes zeroOut() {
        clear();
        UNSAFE.setMemory(startAddr, capacity(), (byte) 0);
        return this;
    }

    @Override
    public Bytes zeroOut(long start, long end) {
        if (start < 0 || end > limit())
            throw new IllegalArgumentException("start: " + start + ", end: " + end);
        if (start >= end)
            return this;
        UNSAFE.setMemory(startAddr + start, end - start, (byte) 0);
        return this;
    }

    @Override
    public Bytes zeroOut(long start, long end, boolean ifNotZero) {
        return ifNotZero ? zeroOutDirty(start, end) : zeroOut(start, end);
    }

    private Bytes zeroOutDirty(long start, long end) {
        if (start < 0 || end > limit())
            throw new IllegalArgumentException("start: " + start + ", end: " + end);
        if (start >= end)
            return this;
        // get unaligned leading bytes
        while (start < end && (start & 7) != 0) {
            byte b = UNSAFE.getByte(startAddr + start);
            if (b != 0)
                UNSAFE.putByte(startAddr + start, (byte) 0);
            start++;
        }
        // check 64-bit aligned access
        while (start < end - 7) {
            long l = UNSAFE.getLong(startAddr + start);
            if (l != 0)
                UNSAFE.putLong(startAddr + start, 0L);
            start++;
        }
        // check unaligned tail
        while (start < end) {
            byte b = UNSAFE.getByte(startAddr + start);
            if (b != 0)
                UNSAFE.putByte(startAddr + start, (byte) 0);
            start++;
        }
        return this;
    }

    @Override
    public int read(@NotNull byte[] bytes, int off, int len) {
        if (len < 0 || off < 0 || off + len > bytes.length)
            throw new IllegalArgumentException();
        long left = remaining();
        if (left <= 0) return -1;
        int len2 = (int) Math.min(len, left);
        UNSAFE.copyMemory(null, positionAddr, bytes, BYTES_OFFSET + off, len2);
        addPosition(len2);
        return len2;
    }

    @Override
    public byte readByte() {
        byte aByte = UNSAFE.getByte(positionAddr);
        addPosition(1);
        return aByte;
    }

    @Override
    public byte readByte(long offset) {
        return UNSAFE.getByte(startAddr + offset);
    }

    @Override
    public void readFully(@NotNull byte[] b, int off, int len) {
        checkArrayOffs(b.length, off, len);
        long left = remaining();
        if (left < len)
            throw new IllegalStateException(new EOFException());
        UNSAFE.copyMemory(null, positionAddr, b, BYTES_OFFSET + off, len);
        addPosition(len);
    }

    @Override
    public void readFully(long offset, byte[] bytes, int off, int len) {
        checkArrayOffs(bytes.length, off, len);
        UNSAFE.copyMemory(null, startAddr + offset, bytes, BYTES_OFFSET + off, len);
    }

    @Override
    public void readFully(@NotNull char[] data, int off, int len) {
        checkArrayOffs(data.length, off, len);
        long bytesOff = off * 2L;
        long bytesLen = len * 2L;
        long left = remaining();
        if (left < bytesLen)
            throw new IllegalStateException(new EOFException());
        UNSAFE.copyMemory(null, positionAddr, data, BYTES_OFFSET + bytesOff, bytesLen);
        addPosition(bytesLen);
    }

    @Override
    public short readShort() {
        short s = UNSAFE.getShort(positionAddr);
        addPosition(2);
        return s;
    }

    @Override
    public short readShort(long offset) {
        return UNSAFE.getShort(startAddr + offset);
    }

    @Override
    public char readChar() {
        char ch = UNSAFE.getChar(positionAddr);
        addPosition(2);
        return ch;
    }

    @Override
    public char readChar(long offset) {
        return UNSAFE.getChar(startAddr + offset);
    }

    @Override
    public int readInt() {
        int i = UNSAFE.getInt(positionAddr);
        addPosition(4);
        return i;
    }

    @Override
    public int readInt(long offset) {
        return UNSAFE.getInt(startAddr + offset);
    }

    @Override
    public int readVolatileInt() {
        int i = UNSAFE.getIntVolatile(null, positionAddr);
        addPosition(4);
        return i;
    }

    @Override
    public int readVolatileInt(long offset) {
        return UNSAFE.getIntVolatile(null, startAddr + offset);
    }

    @Override
    public long readLong() {
        long l = UNSAFE.getLong(positionAddr);
        addPosition(8);
        return l;
    }

    @Override
    public long readLong(long offset) {
        return UNSAFE.getLong(startAddr + offset);
    }

    @Override
    public long readVolatileLong() {
        long l = UNSAFE.getLongVolatile(null, positionAddr);
        addPosition(8);
        return l;
    }

    @Override
    public long readVolatileLong(long offset) {
        return UNSAFE.getLongVolatile(null, startAddr + offset);
    }

    @Override
    public float readFloat() {
        float f = UNSAFE.getFloat(positionAddr);
        addPosition(4);
        return f;
    }

    @Override
    public float readFloat(long offset) {
        return UNSAFE.getFloat(startAddr + offset);
    }

    @Override
    public double readDouble() {
        double d = UNSAFE.getDouble(positionAddr);
        addPosition(8);
        return d;
    }

    @Override
    public double readDouble(long offset) {
        return UNSAFE.getDouble(startAddr + offset);
    }

    @Override
    public void write(int b) {
        UNSAFE.putByte(positionAddr, (byte) b);
        incrementPositionAddr(1);
    }

    @Override
    public void writeByte(long offset, int b) {
        offsetChecks(offset, 1L);
        UNSAFE.putByte(startAddr + offset, (byte) b);
    }

    @Override
    public void write(long offset, @NotNull byte[] bytes) {
        if (offset < 0 || offset + bytes.length > capacity())
            throw new IllegalArgumentException();
        UNSAFE.copyMemory(bytes, BYTES_OFFSET, null, startAddr + offset, bytes.length);
        addPosition(bytes.length);
    }

    @Override
    public void write(byte[] bytes, int off, int len) {
        if (off < 0 || off + len > bytes.length || len > remaining())
            throw new IllegalArgumentException();
        UNSAFE.copyMemory(bytes, BYTES_OFFSET + off, null, positionAddr, len);
        addPosition(len);
    }

    @Override
    public void write(long offset, byte[] bytes, int off, int len) {
        if (offset < 0 || off + len > bytes.length || offset + len > capacity())
            throw new IllegalArgumentException();
        UNSAFE.copyMemory(bytes, BYTES_OFFSET + off, null, startAddr + offset, len);
    }

    @Override
    public void writeShort(int v) {
        positionChecks(positionAddr + 2L);
        UNSAFE.putShort(positionAddr, (short) v);
        positionAddr += 2L;
    }

    private long incrementPositionAddr(long value) {
        positionAddr(positionAddr() + value);
        return positionAddr();
    }

    @Override
    public void writeShort(long offset, int v) {
        offsetChecks(offset, 2L);
        UNSAFE.putShort(startAddr + offset, (short) v);
    }

    @Override
    public void writeChar(int v) {
        positionChecks(positionAddr + 2L);
        UNSAFE.putChar(positionAddr, (char) v);
        positionAddr += 2L;
    }

    void addPosition(long delta) {
        positionAddr(positionAddr() + delta);
    }

    @Override
    public void writeChar(long offset, int v) {
        offsetChecks(offset, 2L);
        UNSAFE.putChar(startAddr + offset, (char) v);
    }

    @Override
    public void writeInt(int v) {
        positionChecks(positionAddr + 4L);
        UNSAFE.putInt(positionAddr, v);
        positionAddr += 4L;
    }

    @Override
    public void writeInt(long offset, int v) {
        offsetChecks(offset, 4L);
        UNSAFE.putInt(startAddr + offset, v);
    }

    @Override
    public void writeOrderedInt(int v) {
        positionChecks(positionAddr + 4L);
        UNSAFE.putOrderedInt(null, positionAddr, v);
        positionAddr += 4L;
    }

    @Override
    public void writeOrderedInt(long offset, int v) {
        offsetChecks(offset, 4L);
        UNSAFE.putOrderedInt(null, startAddr + offset, v);
    }

    @Override
    public boolean compareAndSwapInt(long offset, int expected, int x) {
        offsetChecks(offset, 4L);
        return UNSAFE.compareAndSwapInt(null, startAddr + offset, expected, x);
    }

    @Override
    public void writeLong(long v) {
        positionChecks(positionAddr + 8L);
        UNSAFE.putLong(positionAddr, v);
        positionAddr += 8L;
    }

    @Override
    public void writeLong(long offset, long v) {
        offsetChecks(offset, 8L);
        UNSAFE.putLong(startAddr + offset, v);
    }

    @Override
    public void writeOrderedLong(long v) {
        positionChecks(positionAddr + 8L);
        UNSAFE.putOrderedLong(null, positionAddr, v);
        positionAddr += 8L;
    }

    @Override
    public void writeOrderedLong(long offset, long v) {
        offsetChecks(offset, 8L);
        UNSAFE.putOrderedLong(null, startAddr + offset, v);
    }

    @Override
    public boolean compareAndSwapLong(long offset, long expected, long x) {
        offsetChecks(offset, 8L);
        return UNSAFE.compareAndSwapLong(null, startAddr + offset, expected, x);
    }

    @Override
    public void writeFloat(float v) {
        positionChecks(positionAddr + 4L);
        UNSAFE.putFloat(positionAddr, v);
        positionAddr += 4L;
    }

    @Override
    public void writeFloat(long offset, float v) {
        offsetChecks(offset, 4L);
        UNSAFE.putFloat(startAddr + offset, v);
    }

    @Override
    public void writeDouble(double v) {
        positionChecks(positionAddr + 8L);
        UNSAFE.putDouble(positionAddr, v);
        positionAddr += 8L;
    }

    @Override
    public void writeDouble(long offset, double v) {
        offsetChecks(offset, 8L);
        UNSAFE.putDouble(startAddr + offset, v);
    }

    @Override
    public void readObject(Object object, int start, int end) {
        int len = end - start;
        if (positionAddr + len >= limitAddr)
            throw new IndexOutOfBoundsException("Length out of bounds len: " + len);

        for (; len >= 8; len -= 8) {
            UNSAFE.putLong(object, (long) start, UNSAFE.getLong(positionAddr));
            incrementPositionAddr(8L);
            start += 8;
        }
        for (; len > 0; len--) {
            UNSAFE.putByte(object, (long) start, UNSAFE.getByte(positionAddr));
            incrementPositionAddr(1L);
            start++;
        }
    }

    @Override
    public void writeObject(Object object, int start, int end) {
        int len = end - start;

        for (; len >= 8; len -= 8) {
            positionChecks(positionAddr + 8L);
            UNSAFE.putLong(positionAddr, UNSAFE.getLong(object, (long) start));
            positionAddr += 8;
            start += 8;
        }
        for (; len > 0; len--) {
            positionChecks(positionAddr + 1L);
            UNSAFE.putByte(positionAddr, UNSAFE.getByte(object, (long) start));
            positionAddr++;
            start++;
        }
    }

    @Override
    public boolean compare(long offset, RandomDataInput input, long inputOffset, long len) {
        if (offset < 0 || inputOffset < 0 || len < 0)
            throw new IndexOutOfBoundsException();
        if (offset + len < 0 || offset + len > capacity() || inputOffset + len < 0 ||
                inputOffset + len > input.capacity()) {
            return false;
        }
        long i = 0L;
        for (; i < len - 7L; i += 8L) {
            if (UNSAFE.getLong(startAddr + offset + i) != input.readLong(inputOffset + i))
                return false;
        }
        if (i < len - 3L) {
            if (UNSAFE.getInt(startAddr + offset + i) != input.readInt(inputOffset + i))
                return false;
            i += 4L;
        }
        if (i < len - 1L) {
            if (UNSAFE.getChar(startAddr + offset + i) != input.readChar(inputOffset + i))
                return false;
            i += 2L;
        }
        if (i < len) {
            if (UNSAFE.getByte(startAddr + offset + i) != input.readByte(inputOffset + i))
                return false;
        }
        return true;
    }

    @Override
    public long position() {
        return (positionAddr - startAddr);
    }

    @Override
    public NativeBytes position(long position) {
        if (position < 0 || position > limit())
            throw new IndexOutOfBoundsException("position: " + position + " limit: " + limit());

        positionAddr(startAddr + position);
        return this;
    }

    /**
     * Change the position acknowleging there is no thread safety assumptions. Best effort setting
     * is fine. *
     *
     * @param position to set if we can.
     * @return this
     */
    public NativeBytes lazyPosition(long position) {
        if (position < 0 || position > limit())
            throw new IndexOutOfBoundsException("position: " + position + " limit: " + limit());

        // assume we don't need to no check thread safety.

        positionAddr(startAddr + position);
        return this;
    }

    @Override
    public void write(RandomDataInput bytes, long position, long length) {
        if (length > remaining())
            throw new IllegalArgumentException("Attempt to write " + length + " bytes with " + remaining() + " remaining");
        if (bytes instanceof NativeBytes) {
            UNSAFE.copyMemory(((NativeBytes) bytes).startAddr + position, positionAddr, length);
            skip(length);

        } else {
            super.write(bytes, position, length);
        }
    }

    @Override
    public long capacity() {
        return (capacityAddr - startAddr);
    }

    @Override
    public long remaining() {
        return (limitAddr - positionAddr);
    }

    @Override
    public long limit() {
        return (limitAddr - startAddr);
    }

    @Override
    public NativeBytes limit(long limit) {
        if (limit < 0 || limit > capacity()) {
            throw new IllegalArgumentException("limit: " + limit + " capacity: " + capacity());
        }

        limitAddr = startAddr + limit;
        return this;
    }

    @NotNull
    @Override
    public ByteOrder byteOrder() {
        return ByteOrder.nativeOrder();
    }

    @Override
    public void checkEndOfBuffer() throws IndexOutOfBoundsException {
        if (position() > limit()) {
            throw new IndexOutOfBoundsException(
                    "position is beyond the end of the buffer " + position() + " > " + limit());
        }
    }

    public long startAddr() {
        return startAddr;
    }

    long capacityAddr() {
        return capacityAddr;
    }

    @Override
    protected void cleanup() {
        // TODO nothing to do.
    }

    @Override
    public Bytes load() {
        int pageSize = UNSAFE.pageSize();
        for (long addr = startAddr; addr < capacityAddr; addr += pageSize)
            UNSAFE.getByte(addr);
        return this;
    }

    public void alignPositionAddr(int powerOf2) {
        long value = (positionAddr + powerOf2 - 1) & ~(powerOf2 - 1);
        positionAddr(value);
    }

    public void positionAddr(long positionAddr) {
        positionChecks(positionAddr);
        this.positionAddr = positionAddr;
    }

    void positionChecks(long positionAddr) {
        assert actualPositionChecks(positionAddr);
    }

    boolean actualPositionChecks(long positionAddr) {
        if (positionAddr < startAddr)
            throw new IndexOutOfBoundsException("position before the start by " + (startAddr - positionAddr) + " bytes");
        if (positionAddr > limitAddr)
            throw new IndexOutOfBoundsException("position after the limit by " + (positionAddr - limitAddr) + " bytes");

        return true;
    }

    void offsetChecks(long offset, long len) {
        assert actualOffsetChecks(offset, len);
    }

    boolean actualOffsetChecks(long offset, long len) {
        if (offset < 0L || offset + len > capacity())
            throw new IndexOutOfBoundsException("offset out of bounds: " + offset + ", len: " +
                    len + ", capacity: " + capacity());
        return true;
    }

    public long positionAddr() {
        return positionAddr;
    }

    @Override
    public ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse) {
        return sliceAsByteBuffer(toReuse, null);
    }

    protected ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse, Object att) {
        return ByteBufferReuse.INSTANCE.reuse(positionAddr, (int) remaining(), att, toReuse);
    }

    void address(long address) {
        setStartPositionAddress(address);
    }

    void capacity(long capacity) {
        this.limitAddr = this.capacityAddr = capacity;
    }
}
