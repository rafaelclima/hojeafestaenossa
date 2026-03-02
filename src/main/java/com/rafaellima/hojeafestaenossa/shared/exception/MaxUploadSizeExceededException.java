package com.rafaellima.hojeafestaenossa.shared.exception;

public class MaxUploadSizeExceededException extends ExceptionCustomized {

    public MaxUploadSizeExceededException() {

        super("413", "Arquivo excedeu o tamanho máximo permitido.");

    }

}
