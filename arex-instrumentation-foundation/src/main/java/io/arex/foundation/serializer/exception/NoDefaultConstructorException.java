package io.arex.foundation.serializer.exception;

public class NoDefaultConstructorException extends RuntimeException{
    private static final long serialVersionUID = 13241645613212L;
    private String className;

    public NoDefaultConstructorException() {
        super();
    }

    public NoDefaultConstructorException(String className) {
        super();
        this.className = className;
    }
}
