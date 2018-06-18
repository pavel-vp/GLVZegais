package com.glvz.egais.model;

public enum IncomeRecStatus {

    NEW(1, "Новый"),
    INPROGRESS(2, "В приемке"),
    REJECTED(3, "Отказано"),
    DONE(4, "Принято");

    private int code;
    private String message;

    IncomeRecStatus(int code, String message) {
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
