package com.hisense.codewar.model;



public enum TankGameActionType {
    TANK_ACTION_MOVE('M'), TANK_ACTION_ROTATE('R'), TANK_ACTION_FIRE('F');

    private int value;

    private TankGameActionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;

    }
}
