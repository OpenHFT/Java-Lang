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

abstract class ByteCountListenerPool<T extends SimpleByteCountListener> {
    private final ThreadLocal<T> pool = new ThreadLocal<T>();

    T acquireByteCountListener() {
        T instance = pool.get();
        if (instance == null) {
            pool.set(instance = createInstance());
        }
        instance.reset();
        return instance;
    }

    abstract T createInstance();
}
