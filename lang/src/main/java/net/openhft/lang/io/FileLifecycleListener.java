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
package net.openhft.lang.io;

import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by peter.lawrey on 05/08/2015.
 */
public interface FileLifecycleListener {
    void onEvent(EventType type, File file, long timeInNanos);

    enum EventType {
        NEW,
        MMAP,
        UNMAP,
        GROW,
        SYNC,
        DELETE,
        CLOSE
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
}
