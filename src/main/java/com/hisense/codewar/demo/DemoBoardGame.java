package com.hisense.codewar.demo;

import java.util.List;
import java.util.Random;

import com.hisense.codewar.*;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGame;
import com.hisense.codewar.model.TankGameActionType;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankGamePlayInterface;
import com.hisense.codewar.model.TankMapProjectile;

class DemoBoardGame implements TankGamePlayInterface {
    public int state = 0;
    public int nowx = -1;
    public int nowy = -1;
    public int round = 0;
    @Override
    public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles) {

        int id = tank.getId();
        // System.out.println("Tankid:" + id);
        for (TankGameInfo tmp : tanks) {
            if (tmp.id == id) {
                nowx = tmp.x;
                nowy = tmp.y;
                // System.out.println(" found Tankid:" + id);

                break;
            }
        }

        // printf("pos = (%d, %d)\n", nowx, nowy);
        // System.out.println("nowx:" + nowx + ",nowy:" + nowy);
        if (state == 0 || state == 4) {
            if (nowx >= 1550) {
                state = 1;
                System.out.println("change state to " + state);
            }
        } else if (state == 1) {
            if (nowy >= 850) {
                state = 2;
                System.out.println("change state to " + state);
            }
        } else if (state == 2) {
            if (nowx <= 50) {
                state = 3;
                System.out.println("change state to " + state);
            }
        } else if (state == 3) {
            if (nowy <= 50) {
                state = 4;
                System.out.println("change state to " + state);
            }
        } else {
        }

    }


    private void tankaction(ITtank tank, int deg) {

        round++;
        // printf("move deg = %d\n", deg);

        tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, deg);
        if (round % 10 == 0) {
            Random random = new Random();
            int r = deg + random.nextInt(180);
            tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, r);
            tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
        } else {
            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, deg);
            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, deg);
        }
    }


    @Override
    public void gametick(ITtank tank) {
        if (nowx == -1 || nowy == -1) {
            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, 0);
            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, 0);
            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, 0);
            return;
        }

        if (state == 0) {
            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, 0);
            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, 0);
            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, 0);
        } else {
            tankaction(tank, state * 90);
        }
    }

    public static void main(String[] args) {
        // write your code here
        String recorddir = "E:\\code";
        System.out.println("Hello World!!");
        TankGame game = new TankGame();
        DemoBoardGame player = new DemoBoardGame();

        String token = "56a4314c53183dedfa442eb96f6decf6";
        ITtank tank = game.tank_init("10.18.224.205", 22222, token, player, null);
        game.tank_loop(tank);
    }


	@Override
	public void onstart(int i) {
		// TODO Auto-generated method stub
		
	}
}
