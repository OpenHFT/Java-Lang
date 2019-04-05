package net.openhft.lang.io;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class Bits {

  @NotNull
  protected static final Unsafe UNSAFE;

  public static final String MEMORY_ALIGNED_ACCESS_PROPERTY = "net.openhft.io.memory.aligned.access";
  public static final String BYTE_ORDER_BIGENDIAN_PROPERTY = "net.openhft.io.byte.order.bigEndian";

  public static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();

  /** Unaligned flag. */
  private static final Bits ALIGNED = aligned();

  static {
    try {
      Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
      theUnsafe.setAccessible(true);
      UNSAFE = (Unsafe) theUnsafe.get(null);
    } catch (Exception e) {
      throw new AssertionError(e);
    }

  public static short getShort(long addr) {
    return ALIGNED.getShortByByte(addr);
  }

  public static void putShort(long addr, short v) {
    ALIGNED.putShortByByte(addr, v);
  }

  public static char getChar(long addr) {
    return ALIGNED.getCharByByte(addr);
  }

  public static void putChar(long addr, char v) {
    ALIGNED.putCharByByte(addr, (char) v);
  }

  public static int getInt(long addr) {
    return ALIGNED.getIntByByte(addr);
  }

  public static void putInt(long addr, int v) {
    ALIGNED.putIntByByte(addr, v);
  }

  public static long getLong(long addr) {
    return ALIGNED.getLongByByte(addr);
  }

  public static void putLong(long addr, long v) {
    ALIGNED.putLongByByte(addr, v);
  }

  public static float getFloat(long addr) {
    return Float.intBitsToFloat(ALIGNED.getIntByByte(addr));
  }

  public static void putFloat(long addr, float v) {
    ALIGNED.putIntByByte(addr, Float.floatToIntBits(v));
  }

  public static double getDouble(long addr) {
    return Double.longBitsToDouble(ALIGNED.getLongByByte(addr));
  }

  public static void putDouble(long addr, double v) {
    ALIGNED.putLongByByte(addr, Double.doubleToLongBits(v));
  }

  /**
   * @param addr
   *          Address.
   */
  protected short getShortByByte(long addr) {
    return UNSAFE.getShort(addr);
  }

  /**
   * @param addr
   *          Address.
   * @param val
   *          Value.
   */
  protected void putShortByByte(long addr, short val) {
    UNSAFE.putShort(addr, val);
  }

  /**
   * @param addr
   *          Address.
   */
  protected char getCharByByte(long addr) {
    return UNSAFE.getChar(addr);
  }

  /**
   * @param addr
   *          Address.
   * @param val
   *          Value.
   */
  protected void putCharByByte(long addr, char val) {
    UNSAFE.putChar(addr, (char) val);
  }

  /**
   * @param addr
   *          Address.
   */
  protected int getIntByByte(long addr) {
    return UNSAFE.getInt(addr);
  }

  /**
   * @param addr
   *          Address.
   * @param val
   *          Value.
   */
  protected void putIntByByte(long addr, int val) {
    UNSAFE.putInt(addr, val);
  }

  /**
   * @param addr
   *          Address.
   */
  protected long getLongByByte(long addr) {
    return UNSAFE.getLong(addr);
  }

  /**
   * @param addr
   *          Address.
   * @param val
   *          Value.
   */
  protected void putLongByByte(long addr, long val) {
    UNSAFE.putLong(addr, val);
  }

  /**
   * Returns unaligned flag.
   */
  private static Bits aligned() {
    String arch = System.getProperty("os.arch");
    String model = System.getProperty("sun.arch.data.model");
    boolean aligned = (arch.equals("sparcv9") || arch.equals("sparc")) && model.equals("64");
    if (!aligned)
      aligned = Boolean.getBoolean(MEMORY_ALIGNED_ACCESS_PROPERTY);
    if (aligned) {
      return bigEndian() ? new BitsAlignedBigEndian() : new BitsAligned();
    }
    return new Bits();
  }

  private static boolean bigEndian() {
    if (System.getProperty(BYTE_ORDER_BIGENDIAN_PROPERTY) == null) {
      return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    }
    return Boolean.getBoolean(BYTE_ORDER_BIGENDIAN_PROPERTY);
  }
}