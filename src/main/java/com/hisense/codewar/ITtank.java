package com.hisense.codewar;


import java.util.ArrayList;
import java.util.List;

public class ITtank {
    public String token;
    public String cache;
    public int id;
    public TankGameState state;

    public List<TankGameAction> actions;
    public List<TankMapBlock> blocks;
    public String recorddir;

    public ITtank(String token, String recorddir) {
        this.token = token;
        this.recorddir = recorddir;
        state = TankGameState.sInit;
        actions = new ArrayList<TankGameAction>();
        blocks = new ArrayList<TankMapBlock>();

    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRecordir() {
        return recorddir;
    }

    public void setRecordir(String recorddir) {
        this.recorddir = recorddir;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public void addCache(String cache) {
        this.cache += cache;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TankGameState getState() {
        return state;
    }

    public void setState(TankGameState state) {
        this.state = state;
    }

    public List<TankGameAction> getActions() {
        return actions;
    }

    public void setActions(List<TankGameAction> actions) {
        this.actions = actions;
    }

    public void addAction(TankGameAction action){
        this.actions.add(action);
        while(this.actions.size()>3){
            this.actions.remove(0);
        }
        
    }

    public void tank_action(TankGameActionType action, int arg)
    {
        addAction(new TankGameAction(action.getValue(), arg));
    }

    public List<TankMapBlock> getBlocks() {
        return blocks;
    }


    public void setBlocks(List<TankMapBlock> blocks) {
        this.blocks = blocks;
    }

    public void addBlock(TankMapBlock block) {
        this.blocks.add(block);
    }

    public  void clearBlocks(){
        this.blocks.clear();
    }

    public void clearAction(){
        this.actions.clear();
    }


}
