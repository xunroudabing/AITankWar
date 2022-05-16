package com.hisense.codewar.demo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGame;
import com.hisense.codewar.model.TankGameActionType;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankGamePlayInterface;
import com.hisense.codewar.model.TankMapProjectile;
import com.hisense.codewar.player.DemoPlayer;

public class DemoShooterGame implements TankGamePlayInterface {
	private static final Logger log = LoggerFactory.getLogger(DemoShooterGame.class);
	public Random mRandom = new Random();
	public static final int[] FRIENDS = { 71, 75, 76, 130, 131, 132 };
	public List<Integer> friends = new ArrayList<Integer>();
	public List<TankGameInfo> friendsTANKs = new ArrayList<TankGameInfo>();
	public List<Long> friendsRadius = new ArrayList<Long>();
	public long dest_deg = 0;
	public boolean found = false;
	public int distance = 0;
	public boolean escape = false;
	public int last_shoot = 0;
	public int round = 0;
	public int heading = 0;

	public boolean isFriend(int id) {
		int length = FRIENDS.length;
		for (int i = 0; i < length; i++) {
			if (id == FRIENDS[i]) {
				return true;
			}
		}
		return false;
	}

	public void addFriend(int id, TankGameInfo tank) {
		if (!friends.contains(id)) {
			friends.add(id);
			friendsTANKs.add(tank);
			// System.out.println("addFriend:" + id);
		}
	}

	public void cauluteFriendsR(int nowx, int nowy) {
		for (TankGameInfo tank : friendsTANKs) {
			long radius = getTargetRadius(tank, nowx, nowy);
			friendsRadius.add(radius);
		}

	}

	public boolean mayShootFriends(long deg) {
		for (Long r : friendsRadius) {
			long span = Math.abs(r - deg);
			if (span < 5) {
				return true;
			}
		}
		return false;
	}

	public long getTargetRadius(TankGameInfo target, int nowx, int nowy) {
		long ret = 0;
		if (target.getX() == nowx) {
			if (target.getY() > nowy) {
				ret = 90;
			} else {
				ret = 270;
			}
		} else {
			ret = r2a(Math.atan((float) (target.getY() - nowy) / (float) (target.getX() - nowx)));
		}
		if ((target.getX() < nowx && target.getY() < nowy) || (target.getX() < nowx && target.getY() > nowy)) {
			ret += 180;
		}
		return ret;
	}

	public TankGameInfo getLeader() {
		if (friendsTANKs.size() <= 1) {
			return null;
		}
		friendsTANKs.sort(new Comparator<TankGameInfo>() {

			@Override
			public int compare(TankGameInfo o1, TankGameInfo o2) {
				// TODO Auto-generated method stub
				return o1.id - o2.id;
			}
		});

		return friendsTANKs.get(0);

	}

	public float a2r(int angle) {
		return angle * 3.14159265358979f / 180.0f;
	}

	public long r2a(double radian) {
		return Math.round(radian * 180.0f / 3.14159265358979f);
	}

	@Override
	public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles) {
		friends.clear();
		friendsTANKs.clear();
		friendsRadius.clear();
		int id = tank.getId();
		int nowx = 0;
		int nowy = 0;

		for (TankGameInfo t : tanks) {
			if (t.getId() == id) {
				nowx = t.x;
				nowy = t.y;
				heading = t.getR();
				break;
			}
		}

		// find nearest tank.
		TankGameInfo nearest = null;
		int mindist = 0;

		TankGameInfo leader = getLeader();
		for (TankGameInfo t : tanks) {
			if (t.getId() == id) {
				continue;
			}

			if (isFriend(t.getId())) {
				addFriend(t.getId(), t);
				continue;
			}

			int nx = nowx;
			int ny = nowy;
			if (leader != null) {
				nx = leader.getX();
				ny = leader.getY();
			}

			int dx = t.x - nx;
			int dy = t.y - ny;
			int dist = dx * dx + dy * dy;
			if (mindist == 0 || dist < mindist) {
				mindist = dist;
				nearest = t;
			}
		}

		// 计算好友角度
		cauluteFriendsR(nowx, nowy);
		if (nearest == null) {
			found = false;
			// printf("No enemy found.\n");
			return;
		}
		found = true;
		distance = mindist;
		// System.out.println("[" + id + "]lock target:[" + nearest.id + "],distance:" +
		// distance);
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
		escape = (distance < 15000);
		
		printLog1(tanks);
		printlog2(projectiles);

	}

	@Override
	public void gametick(ITtank tank) {
		round++;

		if (!found) {
			tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, round % 360);

			int dest = (int) dest_deg;
			if (mayShootFriends(dest)) {
				int r = mRandom.nextBoolean() ? 5 : -5;
				dest += r;
				return;
			}
			tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
			return;
		}
		int dest = (int) dest_deg;
		if (mayShootFriends(dest)) {
			int r = mRandom.nextBoolean() ? 8 : -8;
			dest += r;
		}

		else if (distance > 550000) {
			int b = mRandom.nextBoolean() ? 1 : -1;
			int r = mRandom.nextInt(10);
			dest += b * r;

		}else if(distance > 350000 && distance < 550000) {
			int b = mRandom.nextBoolean() ? 1 : -1;
			int r = mRandom.nextInt(6);
			dest += b * r;
		}
		else if (distance > 200000 && distance <= 350000) {
			int b = mRandom.nextBoolean() ? 1 : -1;
			int r = mRandom.nextInt(4);
			dest += b * r;

		}
		
		else if (distance <= 100000) {
			if (Math.abs(heading - dest) < 5) {
				tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
				tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
				tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
				//System.out.println("3 shoot!");
				return;
			}
			
		} 
		
		if (Math.abs(heading - dest) < 5) {
			tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
			//System.out.println("long 3 shoot!");
		}else {
			tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, dest);
		}
		
		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);

//        if (distance > 20000) {
//            tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg);
//        } else {
//            if (escape) {
//                tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 180);
//            } else {
//                tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 90);
//            }
//        }
//    
//        if (round - last_shoot > 30) {
//            tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, (int)dest_deg - 5);
//            tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
//            last_shoot = round;
//        } else {
//            if (distance > 20000) {
//                tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg);
//                tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg);
//            } else {
//                if (escape) {
//                    tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 180);
//                    tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 180);
//                } else {
//                    tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 90);
//                    tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, (int)dest_deg + 90);
//                }
//            }
//        }        

	}

	public static void main(String[] args) {
		// write your code here
		System.out.println("Hello World!!");
		TankGame game1 = new TankGame();
		TankGame game2 = new TankGame();
		TankGame game3 = new TankGame();
		DemoShooterGame player1 = new DemoShooterGame();
		DemoShooterGame player2 = new DemoShooterGame();
		DemoShooterGame player3 = new DemoShooterGame();

		TankGame game4 = new TankGame();
		TankGame game5 = new TankGame();
		TankGame game6 = new TankGame();
		DemoShooterGame player4 = new DemoShooterGame();
		DemoShooterGame player5 = new DemoShooterGame();
		DemoShooterGame player6 = new DemoShooterGame();

		// 1 - d2dba9687037aa47da426a7d66adf9fd
		// 2 - 2507ddd5e51a637d885c279adf346cce
		// 3 - 56a4314c53183dedfa442eb96f6decf6

		// 4 - c6b67c8bac3f803de75ed99edb814eb2
		// 5 - f979a53aafb07394d6ecd97ee9a8c19c
		// 6 - 8d14ae8d8081462912bc2015526bdac9

		String token1 = "d2dba9687037aa47da426a7d66adf9fd";
		ITtank tank1 = game1.tank_init("10.18.224.205", 22222, token1, player1, null);

		String token2 = "2507ddd5e51a637d885c279adf346cce";
		ITtank tank2 = game2.tank_init("10.18.224.205", 22222, token2, player2, null);

		String token3 = "56a4314c53183dedfa442eb96f6decf6";
		ITtank tank3 = game3.tank_init("10.18.224.205", 22222, token3, player3, null);

		String token4 = "c6b67c8bac3f803de75ed99edb814eb2";
		ITtank tank4 = game4.tank_init("10.18.224.205", 22222, token4, player4, null);

		String token5 = "f979a53aafb07394d6ecd97ee9a8c19c";
		ITtank tank5 = game5.tank_init("10.18.224.205", 22222, token5, player5, null);

		String token6 = "8d14ae8d8081462912bc2015526bdac9";
		ITtank tank6 = game6.tank_init("10.18.224.205", 22222, token6, player6, null);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				game1.tank_loop(tank1);

			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				game2.tank_loop(tank2);

			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				game3.tank_loop(tank3);

			}
		}).start();
		
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				game4.tank_loop(tank4);

			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				game5.tank_loop(tank5);

			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				game6.tank_loop(tank6);

			}
		}).start();
	}

	@Override
	public void onstart(int i) {
		// TODO Auto-generated method stub
		
	}
	
	private void printLog1(List<TankGameInfo> tanks) {
		for (TankGameInfo info : tanks) {
			log.debug("#TankGameInfo#" + info.toString());
		}

	}

	private void printlog2(List<TankMapProjectile> projectiles) {
		for (TankMapProjectile p : projectiles) {
			log.debug("#projectiles#" + p.toString());
		}
	}

}
