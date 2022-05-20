package com.hisense.codewar.model;

public enum TankGameState {
    sInit(0), sWait(1), sGaming(2);

    private int value;

    private TankGameState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;

    }
}