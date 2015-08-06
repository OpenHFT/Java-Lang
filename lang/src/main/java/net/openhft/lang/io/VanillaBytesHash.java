package net.openhft.lang.io;

import net.openhft.lang.Maths;

import java.nio.ByteOrder;

public enum VanillaBytesHash implements BytesHasher {
    INSTANCE;

    private static final int TOP_BYTES = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 4 : 0;

    static final int K0 = 0xc5b03135;
    static final int K1 = 0x1d562d7b;
    static final int M0 = 0x49325e2f;
    static final int M1 = 0x32752743;
    static final int M2 = 0xf4bb2e2f;
    static final int M3 = 0x4a6417c9;

    @Override
    public long hash(Bytes bytes, long offset, long limit) {
        int remaining = Maths.toInt(limit - offset, "Hash of a very large data set");
        // use two hashes so that when they are combined the 64-bit hash is more random.
        long h0 = remaining;
        long h1 = 0;
        int i;
        // optimise chunks of 32 bytes but this is the same as the next loop.
        for (i = 0; i < remaining - 31; i += 32) {
            h0 *= K0;
            h1 *= K1;
            long addrI = offset + i;
            long l0 = bytes.readLong(addrI);
            int l0a = bytes.readInt(addrI + TOP_BYTES);
            long l1 = bytes.readLong(addrI + 8);
            int l1a = bytes.readInt(addrI + 8 + TOP_BYTES);
            long l2 = bytes.readLong(addrI + 16);
            int l2a = bytes.readInt(addrI + 16 + TOP_BYTES);
            long l3 = bytes.readLong(addrI + 24);
            int l3a = bytes.readInt(addrI + 24 + TOP_BYTES);

            h0 += (l0 + l1a) * M0 + (l2 + l3a) * M2;
            h1 += (l1 + l0a) * M1 + (l3 + l2a) * M3;
        }
        // perform a hash of the end.
        int left = remaining - i;
        if (left > 0) {
            h0 *= K0;
            h1 *= K1;
            long addrI = offset + i;
            long l0 = bytes.readIncompleteLong(addrI);
            int l0a = (int) (l0 >> 32);
            long l1 = bytes.readIncompleteLong(addrI + 8);
            int l1a = (int) (l1 >> 32);
            long l2 = bytes.readIncompleteLong(addrI + 16);
            int l2a = (int) (l2 >> 32);
            long l3 = bytes.readIncompleteLong(addrI + 24);
            int l3a = (int) (l3 >> 32);

            h0 += (l0 + l1a) * M0 + (l2 + l3a) * M2;
            h1 += (l1 + l0a) * M1 + (l3 + l2a) * M3;
        }
        return Maths.agitate(h0) ^ Maths.agitate(h1);
    }

    @Override
    public long hash(Bytes bytes) {
        return hash(bytes, bytes.position(), bytes.limit());
    }
}
