package net.openhft.lang.values;

import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.DirectStore;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.DataValueClasses;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * Created by peter.lawrey on 06/08/2015.
 */
@Ignore("JLANG-66")
public class EnumValuesTest {
    @Test
    public void testSetNull() {
        BuySellValues value = DataValueClasses.newInstance(BuySellValues.class);
        value.setValue(null);
        assertNull(value.getValue());
    }

    @Test
    public void testBytesMarshallable() {
        BuySellValues value = DataValueClasses.newInstance(BuySellValues.class);
        DirectBytes bytes = DirectStore.allocate(8).bytes();
        ((BytesMarshallable) value).writeMarshallable(bytes);

        BuySellValues value2 = DataValueClasses.newInstance(BuySellValues.class);
        ((BytesMarshallable) value2).readMarshallable(bytes);
        assertNull(value.getValue());
    }

    @Test
    public void testBytesMarshallable2() {
        BuySellValues value = DataValueClasses.newInstance(BuySellValues.class);
        DirectBytes bytes = DirectStore.allocate(8).bytes();
        value.setValue(BuySell.Buy);
        ((BytesMarshallable) value).writeMarshallable(bytes);

        BuySellValues value2 = DataValueClasses.newInstance(BuySellValues.class);
        ((BytesMarshallable) value2).readMarshallable(bytes);
        assertNull(value.getValue());
    }
}

