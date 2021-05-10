package net.stzups.scribbleshare.data.objects.exceptions;

public class DeserializationLengthException extends DeserializationException {
    public DeserializationLengthException(Object object, int length) {
        super("Length of " + object.getClass().getSimpleName() + " is " + length);
    }
}
