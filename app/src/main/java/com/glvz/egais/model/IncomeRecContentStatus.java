package com.glvz.egais.model;

public enum IncomeRecContentStatus {

    NOT_ENTERED(0, "Не принятно"),
    IN_PROGRESS(1, "В приемке"),
    DONE(2, "Принято");

    private int code;
    private String message;

    IncomeRecContentStatus(int code, String message) {
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
