package net.stzups.scribbleshare.util;

import java.util.ArrayList;
import java.util.List;

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
 *             return new DebugString(this)
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
    private static class Property {
        private final String name;
        private final Object value;

        private Property(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    private final Class<?> clazz;
    private List<Property> properties; // will be lazily allocated if needed

    public DebugString(Object object) {
        this(object.getClass());
    }

    public DebugString(Class<?> clazz) {
        this.clazz = clazz;
    }

    public DebugString add(String name, Object value) {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        properties.add(new Property(name, value));
        return this;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(clazz.getSimpleName());
        if (properties != null) {
            stringBuilder.append("{");
            for (Property property : properties) {
                stringBuilder.append(property);
            }
            stringBuilder.append("}");
        }
        return stringBuilder.toString();
    }
}
