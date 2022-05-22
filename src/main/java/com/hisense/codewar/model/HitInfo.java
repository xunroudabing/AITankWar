package com.hisense.codewar.model;

public class HitInfo {
    public int sTankid;
    public int dTankid;

    public HitInfo(int sid, int did){
        this.sTankid = sid;
        this.dTankid = did;
    }

    public int getSTankid(){
        return sTankid;
    }

    public void setSTankid(int tankid){
        this.sTankid = tankid;
    }

    public int getDTankid(){
        return this.dTankid;
    }

    public void setDTankid(int tankid){
        this.dTankid = tankid;
    }


}
