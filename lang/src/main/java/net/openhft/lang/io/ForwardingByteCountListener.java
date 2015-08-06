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

public final class ForwardingByteCountListener extends SimpleByteCountListener {
    private ByteCountListener wrapped;

    public ForwardingByteCountListener() {
    }

    public void setWrapped(ByteCountListener wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void bytesProcessed(long byteCount) {
        super.bytesProcessed(byteCount);
        wrapped.bytesProcessed(byteCount);
    }
}
