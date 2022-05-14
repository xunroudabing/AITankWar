package com.hisense.codewar.demo;

import java.util.List;

import com.hisense.codewar.ITtank;
import com.hisense.codewar.TankGame;
import com.hisense.codewar.TankGameActionType;
import com.hisense.codewar.TankGameInfo;
import com.hisense.codewar.TankGamePlayInterface;
import com.hisense.codewar.TankMapProjectile;

public class DemoCircleGame implements TankGamePlayInterface{

    public int rr = 0;
    public int cnt = 0;




    @Override
    public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void gametick(ITtank tank) {
        tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, rr);
        rr += 5;
        if (rr > 360) {
            rr -= 360;
        }
    
        // int r = rand() % 360;
        tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, rr);
        tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);        
        
    }

    public static void main(String[] args) {
        // write your code here
        System.out.println("Hello World!!");
        TankGame game = new TankGame();
        DemoCircleGame player = new DemoCircleGame();

        String token = "c9d55dd569553ce1b1703cc9999e362a";
        ITtank tank = game.tank_init("10.18.224.205", 22222, token, player, null);
        game.tank_loop(tank);
    }


	@Override
	public void onstart(int i) {
		// TODO Auto-generated method stub
		
	}    

}