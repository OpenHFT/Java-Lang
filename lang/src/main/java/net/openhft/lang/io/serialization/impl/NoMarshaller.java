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

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshaller;

/**
 * Created with IntelliJ IDEA. User: peter Date: 19/09/13 Time: 18:26 To change this template use File | Settings | File
 * Templates.
 */
public enum NoMarshaller implements BytesMarshaller<Void> {
    INSTANCE;

    @Override
    public void write(Bytes bytes, Void aVoid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void read(Bytes bytes) {
        throw new UnsupportedOperationException();
    }
}
