package net.openhft.lang.io;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteOrder;

public class Bits {

    @NotNull
    @SuppressWarnings("ALL")
    public static final Unsafe UNSAFE;

    public static final String MEMORY_ALIGNED_ACCESS_PROPERTY = "net.openhft.io.memory.aligned.access";
    public static final String BYTE_ORDER_BIGENDIAN_PROPERTY = "net.openhft.io.byte.order.bigEndian";

    public static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();

    /**
     * Unaligned flag.
     */
    protected static final boolean ALIGNED = aligned();

    /**
     * Big endian.
     */
    protected static final boolean BIG_ENDIAN = bigEndian();

    static {
        try {
            @SuppressWarnings("ALL")
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static short getShort(long addr) {
        return ALIGNED ? getShortByByte(addr, BIG_ENDIAN) : UNSAFE.getShort(addr);
    }

    public static void putShort(long addr, short v) {
        if (ALIGNED)
            putShortByByte(addr, v, BIG_ENDIAN);
        else
            UNSAFE.putShort(addr, v);
    }

    public static char getChar(long addr) {
        return ALIGNED ? getCharByByte(addr, BIG_ENDIAN) : UNSAFE.getChar(addr);
    }

    public static void putChar(long addr, char v) {
        if (ALIGNED)
            putCharByByte(addr, (char) v, BIG_ENDIAN);
        else
            UNSAFE.putChar(addr, (char) v);
    }

    public static int getInt(long addr) {
        return ALIGNED ? getIntByByte(addr, BIG_ENDIAN) : UNSAFE.getInt(addr);
    }

    public static void putInt(long addr, int v) {
        if (ALIGNED)
            putIntByByte(addr, v, BIG_ENDIAN);
        else
            UNSAFE.putInt(addr, v);
    }

    public static long getLong(long addr) {
        return ALIGNED ? getLongByByte(addr, BIG_ENDIAN) : UNSAFE.getLong(addr);
    }

    public static void putLong(long addr, long v) {
        if (ALIGNED)
            putLongByByte(addr, v, BIG_ENDIAN);
        else
            UNSAFE.putLong(addr, v);
    }

    public static float getFloat(long addr) {
        return ALIGNED ? Float.intBitsToFloat(getIntByByte(addr, BIG_ENDIAN)) : UNSAFE.getFloat(addr);
    }

    public static void putFloat(long addr, float v) {
        if (ALIGNED)
            putIntByByte(addr, Float.floatToIntBits(v), BIG_ENDIAN);
        else
            UNSAFE.putFloat(addr, v);
    }

    public static double getDouble(long addr) {
        return ALIGNED ? Double.longBitsToDouble(getLongByByte(addr, BIG_ENDIAN)) : UNSAFE.getDouble(addr);
    }

    public static void putDouble(long addr, double v) {
        if (ALIGNED)
            putLongByByte(addr, Double.doubleToLongBits(v), BIG_ENDIAN);
        else
            UNSAFE.putDouble(addr, v);
    }

    /**
     * @param addr      Address.
     * @param bigEndian Order of value bytes in memory. If {@code true} - big-endian, otherwise little-endian.
     */
    private static short getShortByByte(long addr, boolean bigEndian) {
        if (bigEndian)
            return (short) (UNSAFE.getByte(addr) << 8 | (UNSAFE.getByte(addr + 1) & 0xff));
        else
            return (short) (UNSAFE.getByte(addr + 1) << 8 | (UNSAFE.getByte(addr) & 0xff));
    }

    /**
     * @param addr      Address.
     * @param val       Value.
     * @param bigEndian Order of value bytes in memory. If {@code true} - big-endian, otherwise little-endian.
     */
    private static void putShortByByte(long addr, short val, boolean bigEndian) {
        if (bigEndian) {
            UNSAFE.putByte(addr, (byte) (val >> 8));
            UNSAFE.putByte(addr + 1, (byte) val);
        } else {
            UNSAFE.putByte(addr + 1, (byte) (val >> 8));
            UNSAFE.putByte(addr, (byte) val);
        }
    }

    /**
     * @param addr      Address.
     * @param bigEndian Order of value bytes in memory. If {@code true} - big-endian, otherwise little-endian.
     */
    private static char getCharByByte(long addr, boolean bigEndian) {
        if (bigEndian)
            return (char) (UNSAFE.getByte(addr) << 8 | (UNSAFE.getByte(addr + 1) & 0xff));
        else
            return (char) (UNSAFE.getByte(addr + 1) << 8 | (UNSAFE.getByte(addr) & 0xff));
    }

    /**
     * @param addr      Address.
     * @param val       Value.
     * @param bigEndian Order of value bytes in memory. If {@code true} - big-endian, otherwise little-endian.
     */
    private static void putCharByByte(long addr, char val, boolean bigEndian) {
        if (bigEndian) {
            UNSAFE.putByte(addr, (byte) (val >> 8));
            UNSAFE.putByte(addr + 1, (byte) val);
        } else {
            UNSAFE.putByte(addr + 1, (byte) (val >> 8));
            UNSAFE.putByte(addr, (byte) val);
        }
    }

    /**
     * @param addr      Address.
     * @param bigEndian Order of value bytes in memory. If {@code true} - big-endian, otherwise little-endian.
     */
    private static int getIntByByte(long addr, boolean bigEndian) {
        if (bigEndian) {
            return (((int) UNSAFE.getByte(addr)) << 24) | (((int) UNSAFE.getByte(addr + 1) & 0xff) << 16)
                    | (((int) UNSAFE.getByte(addr + 2) & 0xff) << 8) | (((int) UNSAFE.getByte(addr + 3) & 0xff));
        } else {
            return (((int) UNSAFE.getByte(addr + 3)) << 24) | (((int) UNSAFE.getByte(addr + 2) & 0xff) << 16)
                    | (((int) UNSAFE.getByte(addr + 1) & 0xff) << 8) | (((int) UNSAFE.getByte(addr) & 0xff));
        }
    }

    /**
     * @param addr      Address.
     * @param val       Value.
     * @param bigEndian Order of value bytes in memory. If {@code true} - big-endian, otherwise little-endian.
     */
    private static void putIntByByte(long addr, int val, boolean bigEndian) {
        if (bigEndian) {
            UNSAFE.putByte(addr, (byte) (val >> 24));
            UNSAFE.putByte(addr + 1, (byte) (val >> 16));
            UNSAFE.putByte(addr + 2, (byte) (val >> 8));
            UNSAFE.putByte(addr + 3, (byte) (val));
        } else {
            UNSAFE.putByte(addr + 3, (byte) (val >> 24));
            UNSAFE.putByte(addr + 2, (byte) (val >> 16));
            UNSAFE.putByte(addr + 1, (byte) (val >> 8));
            UNSAFE.putByte(addr, (byte) (val));
        }
    }

    /**
     * @param addr      Address.
     * @param bigEndian Order of value bytes in memory. If {@code true} - big-endian, otherwise little-endian.
     */
    private static long getLongByByte(long addr, boolean bigEndian) {
        if (bigEndian) {
            return (((long) UNSAFE.getByte(addr)) << 56) | (((long) UNSAFE.getByte(addr + 1) & 0xff) << 48)
                    | (((long) UNSAFE.getByte(addr + 2) & 0xff) << 40) | (((long) UNSAFE.getByte(addr + 3) & 0xff) << 32)
                    | (((long) UNSAFE.getByte(addr + 4) & 0xff) << 24) | (((long) UNSAFE.getByte(addr + 5) & 0xff) << 16)
                    | (((long) UNSAFE.getByte(addr + 6) & 0xff) << 8) | (((long) UNSAFE.getByte(addr + 7) & 0xff));
        } else {
            return (((long) UNSAFE.getByte(addr + 7)) << 56) | (((long) UNSAFE.getByte(addr + 6) & 0xff) << 48)
                    | (((long) UNSAFE.getByte(addr + 5) & 0xff) << 40) | (((long) UNSAFE.getByte(addr + 4) & 0xff) << 32)
                    | (((long) UNSAFE.getByte(addr + 3) & 0xff) << 24) | (((long) UNSAFE.getByte(addr + 2) & 0xff) << 16)
                    | (((long) UNSAFE.getByte(addr + 1) & 0xff) << 8) | (((long) UNSAFE.getByte(addr) & 0xff));
        }
    }

    /**
     * @param addr      Address.
     * @param val       Value.
     * @param bigEndian Order of value bytes in memory. If {@code true} - big-endian, otherwise little-endian.
     */
    private static void putLongByByte(long addr, long val, boolean bigEndian) {
        if (bigEndian) {
            UNSAFE.putByte(addr, (byte) (val >> 56));
            UNSAFE.putByte(addr + 1, (byte) (val >> 48));
            UNSAFE.putByte(addr + 2, (byte) (val >> 40));
            UNSAFE.putByte(addr + 3, (byte) (val >> 32));
            UNSAFE.putByte(addr + 4, (byte) (val >> 24));
            UNSAFE.putByte(addr + 5, (byte) (val >> 16));
            UNSAFE.putByte(addr + 6, (byte) (val >> 8));
            UNSAFE.putByte(addr + 7, (byte) (val));
        } else {
            UNSAFE.putByte(addr + 7, (byte) (val >> 56));
            UNSAFE.putByte(addr + 6, (byte) (val >> 48));
            UNSAFE.putByte(addr + 5, (byte) (val >> 40));
            UNSAFE.putByte(addr + 4, (byte) (val >> 32));
            UNSAFE.putByte(addr + 3, (byte) (val >> 24));
            UNSAFE.putByte(addr + 2, (byte) (val >> 16));
            UNSAFE.putByte(addr + 1, (byte) (val >> 8));
            UNSAFE.putByte(addr, (byte) (val));
        }
    }

    /**
     * Returns unaligned flag.
     */
    private static boolean aligned() {
        String arch = System.getProperty("os.arch");
        String model = System.getProperty("sun.arch.data.model");
        boolean res = (arch.equals("sparcv9") || arch.equals("sparc")) && model.equals("64");
        if (!res)
            res = Boolean.getBoolean(MEMORY_ALIGNED_ACCESS_PROPERTY);
        return res;
    }

    private static boolean bigEndian() {
        boolean res = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
        if (!res)
            res = Boolean.getBoolean(BYTE_ORDER_BIGENDIAN_PROPERTY);
        return res;
    }
}