package com.glvz.egais.model;

public enum IncomeRecContentStatus {

    NOT_ENTERED(0, "Не принятно"),
    BY_POSITION(1, "Принято позицией"),
    BY_BOX(2, "Принято через коробку");

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
