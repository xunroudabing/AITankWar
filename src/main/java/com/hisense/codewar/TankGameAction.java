package com.hisense.codewar;

public class TankGameAction {
    public int action;
    public int arg;

    public TankGameAction(int c, int a){
        action = c;
        arg = a;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getArg() {
        return arg;
    }

    public void setArg(int arg) {
        this.arg = arg;
    }
}
