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
public class StringValueTest {
    @Test
    public void testSetNull() {
        StringValue value = DataValueClasses.newInstance(StringValue.class);
        value.setValue(null);
        assertNull(value.getValue());
    }

    @Test
    public void testBytesMarshallable() {
        StringValue value = DataValueClasses.newInstance(StringValue.class);
        DirectBytes bytes = DirectStore.allocate(8).bytes();
        ((BytesMarshallable) value).writeMarshallable(bytes);

        StringValue value2 = DataValueClasses.newInstance(StringValue.class);
        ((BytesMarshallable) value2).readMarshallable(bytes);
        assertNull(value.getValue());
    }
}