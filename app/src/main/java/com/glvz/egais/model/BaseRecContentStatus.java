package com.glvz.egais.model;

public enum BaseRecContentStatus {

    NOT_ENTERED(0, "Не принятно"),
    IN_PROGRESS(1, "В приемке"),
    DONE(2, "Принято"),
    REJECTED(3, "Отказано");

    private int code;
    private String message;

    BaseRecContentStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
