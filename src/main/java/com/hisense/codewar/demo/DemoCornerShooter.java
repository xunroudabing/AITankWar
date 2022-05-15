package com.hisense.codewar.demo;

import java.util.List;
import java.util.Random;

import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGame;
import com.hisense.codewar.model.TankGameActionType;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankGamePlayInterface;
import com.hisense.codewar.model.TankMapProjectile;

public class DemoCornerShooter implements TankGamePlayInterface {
	public long dest_deg = 0;
	public boolean found = false;
	public int distance = 0;
	public boolean escape = false;

	public int state = 0;
	public int nowx = -1;
	public int nowy = -1;
	public int round = 0;
	
	public Random random = new Random();
	public float a2r(int angle) {
		return angle * 3.14159265358979f / 180.0f;
	}

	public long r2a(double radian) {
		return Math.round(radian * 180.0f / 3.14159265358979f);
	}

	@Override
	public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles) {
		boolean canmove = true;
		int id = tank.getId();
		// System.out.println("Tankid:" + id);
		for (TankGameInfo tmp : tanks) {
			if (tmp.id == id) {
				if (tmp.x == nowx && tmp.y == nowy) {
					canmove = false;
				}
				nowx = tmp.x;
				nowy = tmp.y;
				// System.out.println(" found Tankid:" + id);
				break;
			}
		}
		if (nowx < 1550 || nowy < 850) {
			if (canmove) {
				state = 0;
			} else {
				System.out.println("无法移动，开始攻击");
				state = 2;
			}
		}
		// printf("pos = (%d, %d)\n", nowx, nowy);
		// System.out.println("nowx:" + nowx + ",nowy:" + nowy);
		if (state == 0) {
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
			System.out.println("已到达角落，开始锁定攻击");
			// find nearest tank.
			TankGameInfo nearest = null;
			int mindist = 0;

			for (TankGameInfo t : tanks) {
				if (t.getId() == id) {
					continue;
				}

//				if (isFriend(t.getId())) {
//					continue;
//				}

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
				dest_deg = r2a(Math.atan((float) (nearest.getY() - nowy) / (float) (nearest.getX() - nowx)));
			}
			if ((nearest.getX() < nowx && nearest.getY() < nowy) || (nearest.getX() < nowx && nearest.getY() > nowy)) {
				dest_deg += 180;
			}
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
		round++;
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
		} else if (state == 1) {
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, state * 90);
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, state * 90);
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, state * 90);
		} else if (state == 2) {

			if (!found) {
				tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, round % 360);
				tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, (int) dest_deg);
				return;
			}
			
			if(distance > 800) {
				//散射
				boolean a = random.nextBoolean();
				int r = random.nextInt(5);
				int seed = a ? r : (r * -1);
				tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, (int) dest_deg + seed);
				System.out.println("distance = " + distance + ",大于800，散射");
				
			}else {
				System.out.println("distance = " + distance);
				tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, (int) dest_deg);
			}
			tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, (int) dest_deg);
			tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
			tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
		}
	}

	public static void main(String[] args) {
		// write your code here
		String recorddir = "E:\\code";
		System.out.println("Hello World!!");
		TankGame game = new TankGame();
		DemoCornerShooter player = new DemoCornerShooter();

		String token = "56a4314c53183dedfa442eb96f6decf6";
		ITtank tank = game.tank_init("10.18.224.205", 22222, token, player, null);
		game.tank_loop(tank);
	}

	@Override
	public void onstart(int i) {
		// TODO Auto-generated method stub
		
	}

}
