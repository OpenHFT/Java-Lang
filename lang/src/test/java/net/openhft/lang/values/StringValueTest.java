/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.values;

import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.DirectStore;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.DataValueClasses;
import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * Created by peter.lawrey on 06/08/2015.
 */
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

        bytes.clear();

        StringValue value2 = DataValueClasses.newInstance(StringValue.class);
        // to ensure, in assert below, that readMarshallable indeed reads and sets null
        value2.setValue("foo");
        ((BytesMarshallable) value2).readMarshallable(bytes);
        assertNull(value2.getValue());
    }
}