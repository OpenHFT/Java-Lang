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

/**
 * A listener to get notified about the number of bytes read/written by the
 * methods on {@link RandomDataInput} / {@link RandomDataOutput} which consume /
 * produce a variable number of bytes (such as
 * {@link RandomDataInput#readUTFΔ(long)} or
 * {@link RandomDataOutput#writeStopBit(long)). The
 * {@link #bytesProcessed(long)} method can be called multiple times during one
 * operation. In this case the individual counts need to be summed up to find
 * the total count.
 */
public interface ByteCountListener {
    void bytesProcessed(long byteCount);
}
