package net.stzups.scribbleshare.data.objects.exceptions;

public class DeserializationTypeException extends DeserializationException {
    public DeserializationTypeException(Class<?> clazz, int id) {
        super("Unknown " + clazz.getSimpleName() + " for given id " + id);
    }
}
