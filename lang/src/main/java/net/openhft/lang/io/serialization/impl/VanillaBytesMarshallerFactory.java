/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.io.serialization.BytesMarshallerFactory;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author peter.lawrey
 */
public class VanillaBytesMarshallerFactory implements BytesMarshallerFactory {

    private final Map<Class, BytesMarshaller> marshallerMap = new LinkedHashMap<Class, BytesMarshaller>();

    //    private final Map<Class, BytesMarshaller> marshallerTextMap = new LinkedHashMap<Class, BytesMarshaller>();
    {
        BytesMarshaller stringMarshaller = new StringMarshaller(16 * 1024);
        marshallerMap.put(String.class, stringMarshaller);
        marshallerMap.put(CharSequence.class, stringMarshaller);
        marshallerMap.put(Class.class, new ClassMarshaller(Thread.currentThread().getContextClassLoader()));
        marshallerMap.put(Date.class, new DateMarshaller(10191));
    }

    @NotNull
    @SuppressWarnings("unchecked")
    @Override
    public <E> BytesMarshaller<E> acquireMarshaller(@NotNull Class<E> eClass, boolean create) {
        BytesMarshaller em = marshallerMap.get(eClass);
        if (em == null)
            if (eClass.isEnum())
                marshallerMap.put(eClass, em = new VanillaBytesMarshaller(eClass, null));
            else if (BytesMarshallable.class.isAssignableFrom(eClass))
                marshallerMap.put(eClass, em = new BytesMarshallableMarshaller((Class) eClass));
            else if (Externalizable.class.isAssignableFrom(eClass))
                marshallerMap.put(eClass, em = new ExternalizableMarshaller((Class) eClass));
            else {
                try {
                    marshallerMap.put(eClass, em = new GenericEnumMarshaller<E>(eClass, 1000));
                } catch (Exception e) {
                    marshallerMap.put(eClass, em = NoMarshaller.INSTANCE);
                }
            }
        return em;
    }
}
