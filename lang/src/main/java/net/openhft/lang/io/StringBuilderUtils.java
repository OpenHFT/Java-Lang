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

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

import static java.lang.Character.toLowerCase;

/**
 * Created by Rob Austin
 */
public enum StringBuilderUtils {
    ;

    private static final Field SB_VALUE, SB_COUNT;
    private static final long SB_COUNT_OFFSET;

    static {
        try {
            SB_VALUE = Class.forName("java.lang.AbstractStringBuilder").getDeclaredField("value");
            SB_VALUE.setAccessible(true);
            SB_COUNT = Class.forName("java.lang.AbstractStringBuilder").getDeclaredField("count");
            SB_COUNT.setAccessible(true);
            SB_COUNT_OFFSET = NativeBytes.UNSAFE.objectFieldOffset(SB_COUNT);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static boolean endsWith(@NotNull final CharSequence source,
                                   @NotNull final String endsWith) {
        for (int i = 1; i <= endsWith.length(); i++) {
            if (toLowerCase(source.charAt(source.length() - i)) !=
                    toLowerCase(endsWith.charAt(endsWith.length() - i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isEqual(CharSequence s, CharSequence cs) {
        if (s == null) return false;
        if (s.length() != cs.length()) return false;
        for (int i = 0; i < cs.length(); i++)
            if (s.charAt(i) != cs.charAt(i))
                return false;
        return true;
    }

    public static String toString(Object o) {
        return o == null ? null : o.toString();
    }

    public static char[] extractChars(StringBuilder sb) {
        try {
            return (char[]) SB_VALUE.get(sb);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public static void setCount(StringBuilder sb, int count) {
        NativeBytes.UNSAFE.putInt(sb, SB_COUNT_OFFSET, count);
    }
}
