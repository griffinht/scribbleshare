package net.stzups.scribbleshare.data.objects.exceptions;

public class DeserializationTypeException extends DeserializationException {
    public DeserializationTypeException(Class<?> clazz, Object type) {
        super("Unknown " + clazz.getSimpleName() + " for " + type);
    }
}
