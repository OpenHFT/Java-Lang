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

package net.openhft.lang.thread;

import org.junit.Test;

/**
 * Created by peter.lawrey on 11/12/14.
 */
public class LightPauserTest {
    @Test
    public void testLightPauser() throws InterruptedException {
        final LightPauser pauser = new LightPauser(100 * 1000, 100 * 1000);
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!Thread.interrupted())
                    pauser.pause();
            }
        };
        thread.start();

        for (int t = 0; t < 3; t++) {
            long start = System.nanoTime();
            int runs = 10000000;
            for (int i = 0; i < runs; i++)
                pauser.unpause();
            long time = System.nanoTime() - start;
            System.out.printf("Average time to unpark was %,d ns%n", time / runs);
            Thread.sleep(20);
        }
        thread.interrupt();
    }
}
