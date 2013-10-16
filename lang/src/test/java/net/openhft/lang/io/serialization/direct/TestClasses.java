package net.openhft.lang.io.serialization.direct;

import java.util.*;

class TestClasses {

    public static class InstanceOnlyNoStaticFields {
        public int intField;
        protected String stringField;
        private Object objectField;
    }

    public static class HasInstanceAndStaticFields {
        public static int staticIntField;
        protected static String staticStringField;
        private static Object staticObjectField;

        public int intField;
        protected String stringField;
        private Object objectField;
    }

    public static class BaseClass {
        private static double staticBaseDoubleField;

        protected List<String> baseListField;
    }

    public static class LevelOneDerivedClass extends BaseClass {
        protected static Object staticLevelOneObjectField;

        public long levelOneLongField;
    }

    public static class LevelTwoDerivedClass extends LevelOneDerivedClass {
        public static String staticLevelTwoStringField;

        final int levelTwoDerivedIntField = 0;
    }

    public static class MixedFields {
        public static int staticIntField ;
        public static double[] staticDoubleArray;

        public static List<String> staticStringList = Collections.singletonList("S1");
        public static Object[] staticObjectArray = {new Object()};

        public int intField;
        public byte byteField;
        public short shortField;
        public long longField;
        public double[] doubleArray;

        public List<String> stringList;
        public Object[] objectArray;

        transient short transientShort;
        public transient Object transientObject;
    }

    public static class Primitives1 {
        public int a;
    }

    public static class Primitives2 {
        public int a, b;
    }

    public static class Primitives3 {
        public int a, b;
        public short c;
    }

    public static class Primitives4 {
        public boolean a;
    }

    public static class Primitives5 {
        public boolean a;
        public long b;
    }

    public static class Primitives6 {
        public boolean a;
        public int b;
        public short c;
        public long d;
        public double e;
    }
}
