package com.hisense.codewar.demo;

import java.util.List;
import java.util.Random;

import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGame;
import com.hisense.codewar.model.TankGameActionType;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankGamePlayInterface;
import com.hisense.codewar.model.TankMapProjectile;

public class DemoCrazyGame implements TankGamePlayInterface{
    public int round = 0;
    public int rr = 0;    

    @Override
    public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void gametick(ITtank tank) {
        // TODO Auto-generated method stub

        if (round % 100 == 0) {
            Random random = new Random();

            rr = random.nextInt(360);

        }
    
        round++;
    
        tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, rr);
    
        if (round % 5 == 0) {
            Random random = new Random();
            int r = random.nextInt(360);            
            tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, r);
            tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
        } else {
            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, rr);
            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, rr);
        }        
        
    }


    public static void main(String[] args) {
        // write your code here
        System.out.println("Hello World!!");
        TankGame game = new TankGame();
        DemoCrazyGame player = new DemoCrazyGame();
        //c9d55dd569553ce1b1703cc9999e362a
        //d2dba9687037aa47da426a7d66adf9fd
        String token = "d2dba9687037aa47da426a7d66adf9fd";
        ITtank tank = game.tank_init("10.18.224.205", 22222, token, player, null);
        game.tank_loop(tank);
    }

	@Override
	public void onstart(int i) {
		// TODO Auto-generated method stub
		
	}   


}
