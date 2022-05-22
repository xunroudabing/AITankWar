package com.hisense.codewar.model;

import java.util.List;

public interface TankGamePlayInterface {
    public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles, int r, List<HitInfo> hits);
    public void gametick(ITtank tank);
    public void gamestart(ITtank tank);
    public void gameend(ITtank tank);

}
