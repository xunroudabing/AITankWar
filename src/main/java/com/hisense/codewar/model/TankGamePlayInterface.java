package com.hisense.codewar.model;

import java.util.List;

public interface TankGamePlayInterface {
	public void onstart(int i);
    public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles);
    public void gametick(ITtank tank);

}
