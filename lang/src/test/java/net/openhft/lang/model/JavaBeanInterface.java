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

package net.openhft.lang.model;

import net.openhft.lang.model.constraints.MaxSize;

public interface JavaBeanInterface {
    void busyLockRecord() throws InterruptedException;

    boolean tryLockRecord();

    void unlockRecord();

    boolean getFlag();

    void setFlag(boolean flag);

    byte getByte();

    void setByte(byte b);

    short getShort();

    void setShort(short s);

    char getChar();

    void setChar(char ch);

    int getVolatileInt();

    void setOrderedInt(int i);

    int getInt();

    void setInt(int i);

    float getFloat();

    void setFloat(float f);

    long getLong();

    void setLong(long l);

    long addAtomicLong(long toAdd);

    double getDouble();

    void setDouble(double d);

    double addAtomicDouble(double toAdd);

    String getString();

    void setString(@MaxSize(8) String s);

    StringBuilder getUsingString(StringBuilder b);
}
