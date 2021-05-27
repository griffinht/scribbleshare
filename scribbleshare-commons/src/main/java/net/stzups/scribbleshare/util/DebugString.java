package net.stzups.scribbleshare.util;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Queue;

/**
 * Used to format a String returned by a toString override. Use the following mock Car class as an example
 * <pre>
 *     public class Car {
 *         private String make;
 *         private int year;
 *
 *         ...
 *
 *         public String toString() {
 *             return DebugString.get(this)
 *                 .add("make", make)
 *                 .add("year", year)
 *                 .toString();
 *         }
 *     }
 * </pre>
 *
 * Now the <code>toString</code> method will return <code>Car{model=Toyota,year=1999}</code>
 */
public class DebugString {
    private static final String OPEN = "{";
    private static final String CLOSE = "}";
    private static final String SEPARATOR = ", ";
    private static final String EQUALS = "=";

    private static class Property {
        private final String name;
        private final Object value;

        private Property(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            String value;
            if (this.value.getClass().isArray()) {
                value = Arrays.toString((Object[]) this.value);
            } else {
                value = this.value.toString();
            }

            if (name == null) {
                return value;
            } else {
                return name + EQUALS + value;
            }
        }
    }

    private final Class<?> clazz;
    private final String string;
    private Queue<Property> properties; // will be lazily allocated if needed

    private DebugString(Class<?> clazz) {
        this(clazz, null);
    }

    private DebugString(Class<?> clazz, String string) {
        this.clazz = clazz;
        this.string = string;
    }

    public DebugString add(Object value) {
        return add(null, value);
    }

    public DebugString add(String name, Object value) {
        if (properties == null) {
            properties = new ArrayDeque<>();
        }
        properties.add(new Property(name, value));
        return this;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(clazz.getSimpleName());
        if (properties != null) {
            stringBuilder.append(OPEN);
            Iterator<Property> iterator = properties.iterator();
            while (iterator.hasNext()) {
                stringBuilder.append(iterator.next());
                if (iterator.hasNext()) {
                    stringBuilder.append(SEPARATOR);
                }
            }
            if (string != null) {
                stringBuilder.append(string);
            }
            stringBuilder.append(CLOSE);
        }
        return stringBuilder.toString();
    }

    // useless factory methods that are easier to type than new DebugLog
    public static DebugString get(Class<?> clazz) {
        return new DebugString(clazz);
    }

    public static DebugString get(Class<?> clazz, String string) {
        return new DebugString(clazz, string);
    }
}
