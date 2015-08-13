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

package net.openhft.lang;

public abstract class MutableLong {
    private MutableLong() {
        // instantiated only through factory methods
    }

    public abstract void increment();

    public abstract void incrementBy(int by);

    public abstract void incrementBy(MutableLong by);

    public abstract void reset();

    public abstract long get();

    public abstract boolean isDummy();

    public static MutableLong getInstance() {
        return new MutableLong() {
            private long value;

            @Override
            public void increment() {
                value += 1;
            }

            @Override
            public void incrementBy(int by) {
                value += by;
            }

            @Override
            public void incrementBy(MutableLong by) {
                value += by.get();
            }

            @Override
            public void reset() {
                value = 0;
            }

            @Override
            public long get() {
                return value;
            }

            @Override
            public boolean isDummy() {
                return false;
            }
        };
    }

    private static final MutableLong DUMMY_INSTANCE = new MutableLong() {
        @Override
        public void increment() {
            // do nothing
        }

        @Override
        public void incrementBy(int by) {
            // do nothing
        }

        @Override
        public void incrementBy(MutableLong by) {
            // do nothing
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long get() {
            throw new UnsupportedOperationException();
        }

        public boolean isDummy() {
            return true;
        }
    };

    public static MutableLong getDummyInstance() {
        return DUMMY_INSTANCE;
    }

    public static ThreadLocal<MutableLong> getPoolInstance() {
        return new ThreadLocal<MutableLong>() {
            @Override
            public MutableLong get() {
                MutableLong result = super.get();
                result.reset();
                return result;
            }

            @Override
            protected MutableLong initialValue() {
                return MutableLong.getInstance();
            }
        };
    }
}
