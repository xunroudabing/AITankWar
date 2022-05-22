package com.hisense.codewar.demo;

import java.util.List;

import com.hisense.codewar.model.HitInfo;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGame;
import com.hisense.codewar.model.TankGameActionType;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankGamePlayInterface;
import com.hisense.codewar.model.TankMapProjectile;

public class DemoShooterGame implements TankGamePlayInterface{
    
    public long dest_deg = 0;
    public boolean found = false;
    public int distance = 0;
    public boolean escape = false;
    public int last_shoot = 0;
    public int round = 0;

    public float a2r(int angle)
    {
        return angle * 3.14159265358979f / 180.0f;
    }

    public long r2a(double radian)
    {
        return Math.round(radian * 180.0f / 3.14159265358979f);
    }

    @Override
    public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles, int r, List<HitInfo> hits) {
        int id = tank.getId();
        int nowx = 0;
        int nowy = 0;
    
        for (TankGameInfo t : tanks) {
            if (t.getId() == id) {
                nowx = t.x;
                nowy = t.y;
                break;
            }
        }
    
    
        // find nearest tank.
        TankGameInfo nearest = null;
        int mindist = 0;
    
        for (TankGameInfo t : tanks) {
            if (t.getId() == id) {
                continue;
            }
    
            int dx = t.x - nowx;
            int dy = t.y - nowy;
            int dist = dx * dx + dy * dy;
            if (mindist == 0 || dist < mindist) {
                mindist = dist;
                nearest = t;
            }
        }
        if (nearest == null) {
            found = false;
            // printf("No enemy found.\n");
            return;
        }
        found = true;
        distance = mindist;
        if (nearest.getX() == nowx) {
            if (nearest.getY() > nowy) {
                dest_deg = 90;
            } else {
                dest_deg = 270;
            }
        } else {
            dest_deg = r2a(Math.atan((float)(nearest.getY() - nowy) / (float)(nearest.getX() - nowx)));
        }
        if ((nearest.getX() < nowx && nearest.getY() < nowy) || (nearest.getX() < nowx && nearest.getY() > nowy)) {
            dest_deg += 180;
        }
        escape = (distance < 15000);        
        
    }

    @Override
    public void gametick(ITtank tank) {
        round++;

        if (!found) {
            tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, round % 360);
            tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, (int)dest_deg);
            return;
        }
    
        if (distance > 20000) {
            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg);
        } else {
            if (escape) {
                tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 180);
            } else {
                tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 90);
            }
        }
    
        if (round - last_shoot > 30) {
            tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, (int)dest_deg - 5);
            tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
            last_shoot = round;
        } else {
            if (distance > 20000) {
                tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg);
                tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg);
            } else {
                if (escape) {
                    tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 180);
                    tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 180);
                } else {
                    tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 90);
                    tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 90);
                }
            }
        }        
        
    }

    @Override
    public void gamestart(ITtank tank) {    
        System.out.println("game start");
    }

    @Override
    public void gameend(ITtank tank) {    
        System.out.println("game end");
    }

    public static void main(String[] args) {
        // write your code here
        System.out.println("Hello World!!");
        TankGame game = new TankGame();
        DemoShooterGame player = new DemoShooterGame();

        String token = "ABC";
        ITtank tank = game.tank_init("10.18.224.205", 22222, token, player, null);
        game.tank_loop(tank);
    }   


}
