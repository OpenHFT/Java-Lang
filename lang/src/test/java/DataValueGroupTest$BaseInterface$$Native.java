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

package net.openhft.lang;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.Byteable;
import net.openhft.lang.model.Copyable;

import static net.openhft.lang.Compare.calcLongHashCode;
import static net.openhft.lang.Compare.isEqual;

public class DataValueGroupTest$BaseInterface$$Native implements GroupTest.BaseInterface, BytesMarshallable, Byteable, Copyable<GroupTest.BaseInterface> {
    private static final int INT = 0;
    private static final int STR = 4;

    private Bytes _bytes;
    private long _offset;

    public int getInt() {
        return _bytes.readInt(_offset + INT);
    }

    public void setInt(int $) {
        _bytes.writeInt(_offset + INT, $);
    }

    public java.lang.String getStr() {
        return _bytes.readUTFΔ(_offset + STR);
    }

    public void setStr(java.lang.String $) {
        _bytes.writeUTFΔ(_offset + STR, 15, $);
    }

    @Override
    public void copyFrom(GroupTest.BaseInterface from) {
        setInt(from.getInt());
        setStr(from.getStr());
    }

    @Override
    public void writeMarshallable(Bytes out) {
        out.writeInt(getInt());
        out.writeUTFΔ(getStr());
    }
    @Override
    public void readMarshallable(Bytes in) {
        setInt(in.readInt());
        setStr(in.readUTFΔ());
    }
    @Override
    public void bytes(Bytes bytes, long offset) {
       this._bytes = bytes;
       this._offset = offset;
    }
    @Override
    public Bytes bytes() {
       return _bytes;
    }
    @Override
    public long offset() {
        return _offset;
    }
    @Override
    public int maxSize() {
       return 19;
    }
    public int hashCode() {
        long lhc = longHashCode();
        return (int) ((lhc >>> 32) ^ lhc);
    }

    public long longHashCode() {
        return (calcLongHashCode(getInt())) * 10191 +
            calcLongHashCode(getStr());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupTest.BaseInterface)) return false;
        GroupTest.BaseInterface that = (GroupTest.BaseInterface) o;

        if(!isEqual(getInt(), that.getInt())) return false;
        return isEqual(getStr(), that.getStr());
    }

    public String toString() {
        if (_bytes == null) return "bytes is null";
        StringBuilder sb = new StringBuilder();
        sb.append("DataValueGroupTest.BaseInterface{ ");
            sb.append("int= ").append(getInt());
sb.append(", ")
;            sb.append("str= ").append(getStr());
        sb.append(" }");
        return sb.toString();
    }
}
