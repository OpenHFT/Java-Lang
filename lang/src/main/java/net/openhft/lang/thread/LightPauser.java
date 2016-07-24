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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by peter.lawrey on 11/12/14.
 */
public class LightPauser implements Pauser {
    public static final long NO_BUSY_PERIOD = -1;
    public static final long NO_PAUSE_PERIOD = -1;
    private final AtomicBoolean pausing = new AtomicBoolean();
    private final long busyPeriodNS;
    private final long parkPeriodNS;
    private int count;
    private long pauseStart = 0;
    private volatile Thread thread;

    public LightPauser(long busyPeriodNS, long parkPeriodNS) {
        this.busyPeriodNS = busyPeriodNS;
        this.parkPeriodNS = parkPeriodNS;
    }

    @Override
    public void reset() {
        pauseStart = count = 0;
    }

    @Override
    public void pause() {
        pause(parkPeriodNS);
    }

    public void pause(long maxPauseNS) {
        if (busyPeriodNS > 0) {
            if (count++ < 1000)
                return;
            if (pauseStart == 0) {
                pauseStart = System.nanoTime();
                return;
            }
            if (System.nanoTime() < pauseStart + busyPeriodNS)
                return;
        }
        if (maxPauseNS < 10000)
            return;
        thread = Thread.currentThread();
        pausing.set(true);
        doPause(maxPauseNS);
        pausing.set(false);
        reset();
    }

    protected void doPause(long maxPauseNS) {
        LockSupport.parkNanos(Math.max(maxPauseNS, parkPeriodNS));
    }

    @Override
    public void unpause() {
        if (pausing.get())
            LockSupport.unpark(thread);
    }
}
