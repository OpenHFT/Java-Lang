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
