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

public class SimpleByteCountListener implements ByteCountListener {
    private long totalByteCount;

    public final long getByteCount() {
        return totalByteCount;
    }

    public final void reset() {
        totalByteCount = 0;
    }

    public final long getAndResetByteCount() {
        long result = totalByteCount;
        totalByteCount = 0;
        return result;
    }

    @Override
    public void bytesProcessed(long byteCount) {
        totalByteCount += byteCount;
    }
}
