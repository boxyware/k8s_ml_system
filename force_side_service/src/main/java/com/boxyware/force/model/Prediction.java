package com.boxyware.force.model;

public class Prediction {

    private final String side;
    private final String error;

    public Prediction(String side, String error) {

        this.side = side;
        this.error = error;
    }

    public String getSide() {
        return side;
    }

    public String getError() {
        return error;
    }
}