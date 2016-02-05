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

import java.math.BigDecimal;
import java.nio.ByteBuffer;

public class BigDecimalVsDoubleMain {

    private static final String[] NUMBER = {"1000000", "1.1", "1.23456", "12345.67890"};
    private static final Bytes[] IN_BYTES = new Bytes[NUMBER.length];
    public static final Bytes OUT_BYTES;

    static {
        DirectStore store = new DirectStore((NUMBER.length + 1) * 16);
        for (int i = 0; i < NUMBER.length; i++) {
            IN_BYTES[i] = store.bytes((i + 1) * 16, 16);
            IN_BYTES[i].append(NUMBER[i]);
        }
        OUT_BYTES = store.bytes(0, 16);
    }

    static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        Bytes x = ByteBufferBytes.wrap(ByteBuffer.allocateDirect(16));
        x.writeUTFΔ("Hello World");
        System.out.println(x);
        int runs = 5000;

        for (int t = 0; t < 5; t++) {
            long timeD = 0;
            long timeBD = 0;
            long timeB = 0;
            if (t == 0)
                System.out.println("Warming up");
            else if (t == 1)
                System.out.println("Cold code");

            int r = t == 0 ? 20000 : runs;
            for (int i = 0; i < r; i += 3) {
                count++;
                if (count >= NUMBER.length) count = 0;

                if (t > 0)
                    Thread.sleep(1);
                timeB += testDoubleWithBytes();
                timeBD += testBigDecimalWithString();
                timeD += testDoubleWithString();
                if (t > 0)
                    Thread.sleep(1);
                timeD += testDoubleWithString();
                timeB += testDoubleWithBytes();
                timeBD += testBigDecimalWithString();
                if (t > 0)
                    Thread.sleep(1);
                timeBD += testBigDecimalWithString();
                timeD += testDoubleWithString();
                timeB += testDoubleWithBytes();
            }
            System.out.printf("double took %.1f us, BigDecimal took %.1f, double with Bytes took %.1f%n",
                    timeD / 1e3 / r, timeBD / 1e3 / r, timeB / 1e3 / r);
        }
    }

    static volatile double saved;
    static volatile String savedStr;

    public static long testDoubleWithString() {
        long start = System.nanoTime();
        saved = Double.parseDouble(NUMBER[count]);
        savedStr = Double.toString(saved);
        return System.nanoTime() - start;
    }

    public static long testDoubleWithBytes() {
        IN_BYTES[count].position(0);
        OUT_BYTES.position(0);

        long start = System.nanoTime();
        saved = IN_BYTES[count].parseDouble();
        OUT_BYTES.append(saved);
        System.out.println(OUT_BYTES);

        return System.nanoTime() - start;
    }

    static volatile BigDecimal savedBD;

    public static long testBigDecimalWithString() {
        long start = System.nanoTime();
        savedBD = new BigDecimal(NUMBER[count]);
        savedStr = savedBD.toString();
        return System.nanoTime() - start;
    }
}
