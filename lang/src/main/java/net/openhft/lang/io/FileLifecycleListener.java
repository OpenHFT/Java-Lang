package net.openhft.lang.io;

import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by peter.lawrey on 05/08/2015.
 */
public interface FileLifecycleListener {
    enum FileLifecycleListeners implements FileLifecycleListener {
        IGNORE {
            @Override
            public void onFileGrowth(File file, long timeInNanos) {

            }
        },
        CONSOLE {
            @Override
            public void onFileGrowth(File file, long timeInNanos) {
                System.out.println("File growth " + file + " took " + timeInNanos / 1000 / 1e3 + " ms.");
            }
        },
        LOG {
            @Override
            public void onFileGrowth(File file, long timeInNanos) {
                LoggerFactory.getLogger(FileLifecycleListeners.class).info("File growth " + file + " took " + timeInNanos / 1000 / 1e3 + " ms.");
            }
        }
    }

    void onFileGrowth(File file, long timeInNanos);
}
