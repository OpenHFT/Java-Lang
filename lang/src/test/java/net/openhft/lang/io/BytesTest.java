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
