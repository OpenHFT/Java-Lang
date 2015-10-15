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

import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by peter.lawrey on 05/08/2015.
 */
public interface FileLifecycleListener {
    enum EventType {
        NEW,
        MMAP,
        UNMAP,
        GROW,
        SYNC,
        DELETE
    }

    enum FileLifecycleListeners implements FileLifecycleListener {
        IGNORE {
            @Override
            public void onEvent(EventType type, File file, long timeInNanos) {
            }
        },
        CONSOLE {
            @Override
            public void onEvent(EventType type, File file, long timeInNanos) {
                System.out.println(
                        "File " + file + " took " + timeInNanos / 1000 / 1e3 + " ms. to " + type);
            }
        },
        LOG {
            @Override
            public void onEvent(EventType type, File file, long timeInNanos) {
                LoggerFactory.getLogger(FileLifecycleListeners.class).info(
                        "File " + file + " took " + timeInNanos / 1000 / 1e3 + " ms. to " + type);
            }
        }
    }

    void onEvent(EventType type, File file, long timeInNanos);
}
