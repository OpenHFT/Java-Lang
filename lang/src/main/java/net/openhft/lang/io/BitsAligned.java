package net.openhft.lang.io;

@SuppressWarnings("restriction")
public class BitsAligned extends Bits {

  @Override
  protected short getShortByByte(long addr) {
    return (short) (UNSAFE.getByte(addr + 1) << 8 | (UNSAFE.getByte(addr) & 0xff));
  }

  @Override
  protected void putShortByByte(long addr, short val) {
    UNSAFE.putByte(addr + 1, (byte) (val >> 8));
    UNSAFE.putByte(addr, (byte) val);
  }

  @Override
  protected char getCharByByte(long addr) {
    return (char) (UNSAFE.getByte(addr + 1) << 8 | (UNSAFE.getByte(addr) & 0xff));
  }

  @Override
  protected void putCharByByte(long addr, char val) {
    UNSAFE.putByte(addr + 1, (byte) (val >> 8));
    UNSAFE.putByte(addr, (byte) val);
  }

  @Override
  protected int getIntByByte(long addr) {
    return (((int) UNSAFE.getByte(addr + 3)) << 24) | (((int) UNSAFE.getByte(addr + 2) & 0xff) << 16)
        | (((int) UNSAFE.getByte(addr + 1) & 0xff) << 8) | (((int) UNSAFE.getByte(addr) & 0xff));
  }

  @Override
  protected void putIntByByte(long addr, int val) {
    UNSAFE.putByte(addr + 3, (byte) (val >> 24));
    UNSAFE.putByte(addr + 2, (byte) (val >> 16));
    UNSAFE.putByte(addr + 1, (byte) (val >> 8));
    UNSAFE.putByte(addr, (byte) (val));
  }

  @Override
  protected long getLongByByte(long addr) {
    return (((long) UNSAFE.getByte(addr + 7)) << 56) | (((long) UNSAFE.getByte(addr + 6) & 0xff) << 48)
        | (((long) UNSAFE.getByte(addr + 5) & 0xff) << 40) | (((long) UNSAFE.getByte(addr + 4) & 0xff) << 32)
        | (((long) UNSAFE.getByte(addr + 3) & 0xff) << 24) | (((long) UNSAFE.getByte(addr + 2) & 0xff) << 16)
        | (((long) UNSAFE.getByte(addr + 1) & 0xff) << 8) | (((long) UNSAFE.getByte(addr) & 0xff));
  }

  @Override
  protected void putLongByByte(long addr, long val) {
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