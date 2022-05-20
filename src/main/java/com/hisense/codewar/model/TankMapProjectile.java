package com.hisense.codewar.model;

public class TankMapProjectile {
    public  int tankid;
    public int x;
    public int y;
    public int r;

    public TankMapProjectile(int tankid, int x, int y, int r) {
        this.tankid = tankid;
        this.x = x;
        this.y = y;
        this.r = r;
    }

    @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return String.format("TankMapProjectile.bullet[%d,%d]r[%d]->tankid[%d]", x,y,r,tankid);
    }
    
}
