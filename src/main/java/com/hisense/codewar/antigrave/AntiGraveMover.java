package com.hisense.codewar.antigrave;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.algorithm.DodageLvl3Algorithm.HitResult;
import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.data.CombatAttackRadar;
import com.hisense.codewar.data.CombatMovementHelper;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.data.FireHelper;
import com.hisense.codewar.data.Map;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankMapBlock;
import com.hisense.codewar.utils.Utils;

/**
 * 小于0为斥力，大于0为引力 factor=40 友军 -1000最大斥力 障碍物-50
 * 
 * @author hanzheng
 *
 */
public class AntiGraveMover {
	public static void main(String[] args) {
		int nowX = 204;
		int nowY = 198;
		int px = 264;
		int py = 198;
		int power = 100;

		int distance = Utils.distanceTo(nowX, nowY, px, py);
		System.out.println("dis=" + distance + " pow=" + Math.pow(Utils.distanceTo(nowX, nowY, px, py) / FACTOR, 2));
		double force = power / Math.pow(Utils.distanceTo(nowX, nowY, px, py) / FACTOR, 2);
		// Find the bearing from the point to us
		// int angle = normaliseBearing(Math.PI / 2 - Math.atan2(getY() - p.y, getX() -
		// p.x));
		int angle = Utils.angleTo(px, py, nowX, nowY);
		double angleToR = Utils.a2r(angle);
		// Add the components of this force to the total force in their respective
		// directions
		int xforce = (int) (Math.cos(angleToR) * force);
		int yforce = (int) (Math.sin(angleToR) * force);

		System.out.println("force=" + force + ",xforce=" + xforce + ",yforce=" + yforce);

		System.out.println(angle);

		int tx = nowX - xforce;
		int ty = nowY - yforce;

		int r = Utils.angleTo(nowX, nowY, tx, ty);
		System.out.println(r);

		int j = 0;
		for (int i = 0; i < 4; i++) {
			j += 45 + 45 * i;
			System.out.println(j);
		}
//		int nowX = 204;
//		int nowY = 198;
//		int xforce = 0;
//		int yforce = 0;
//		List<GravePoint> blocks = new ArrayList<GravePoint>();
//		double force = 0;
//		for (GravePoint p : blocks) {
//			// 距离越远，力越小
//			force = p.getPower() / Math.pow(Utils.distanceTo(nowX, nowY, p.getX(), p.getY()) / FACTOR, 2);
//			// Find the bearing from the point to us
//			int angle = Utils.angleTo(p.getX(), p.getY(), nowX, nowY);
//			double angleToR = Utils.a2r(angle);
//			// Add the components of this force to the total force in their respective
//			// directions
//			xforce += Math.cos(angleToR) * force;
//			yforce += Math.sin(angleToR) * force;
//			System.out.println("force=" + force + ",xforce=" + xforce + ",yforce=" + yforce);
//		}

	}

	private Position mLastPosition;
	private boolean isStupid = false;
	private int stupidTimer = 0;
	private int mTurn = 0;
	private int mTick = 0;
	private Random mRandom = new Random();
	private FireHelper mFireHelper;
	private List<GravePoint> mStupidPoints;
	private List<GravePoint> mGravePoints;
	private CombatAttackRadar mAttackRadar;
	private CombatRealTimeDatabase mDatabase;
	private CombatMovementHelper mHelper;
	private static final int FRIEND_POWER = 1000;
	private static final int BLOCK_POWER = -10;
	private static final int FACTOR = 45;
	private static final Logger log = LoggerFactory.getLogger(AntiGraveMover.class);

	public AntiGraveMover(CombatRealTimeDatabase database, CombatAttackRadar radar, CombatMovementHelper helper,
			FireHelper fireHelper) {
		mDatabase = database;
		mAttackRadar = radar;
		mHelper = helper;
		mFireHelper = fireHelper;
		mGravePoints = new ArrayList<GravePoint>();
		mStupidPoints = new ArrayList<GravePoint>();

	}

	public void reset() {
		stupidTimer = 0;
		mTurn = 0;
		mTick = 0;
		isStupid = false;
		mLastPosition = null;
	}

	public void antiGraveMove() {

		double force = 0;
		int xforce = 0;
		int yforce = 0;
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int tankid = mDatabase.getMyTankId();
		for (GravePoint p : mGravePoints) {
			// 距离越远，力越小
			force = p.getPower() / Math.pow(Utils.distanceTo(nowX, nowY, p.getX(), p.getY()) / FACTOR, 2);
			// Find the bearing from the point to us
			int angle = Utils.angleTo(p.getX(), p.getY(), nowX, nowY);
			double angleToR = Utils.a2r(angle);

			xforce += Math.cos(angleToR) * force;
			yforce += Math.sin(angleToR) * force;
//			log.debug(String.format(
//					"[AntiGraveMove]tankid[%d]enemy[%d,%d]me[%d,%d]power[%d]angle[%d][force=%f,xforce=%d,yforce=%d]",
//					tankid, p.getX(), p.getY(), nowX, nowY, p.getPower(), angle, force, xforce, yforce));
		}
		// blocks
		Map map = mDatabase.getMap();
		xforce += 5500 / Math.pow(Utils.distanceTo(nowX, nowY, map.maxX, nowY) / FACTOR, 3);
		xforce -= 5500 / Math.pow(Utils.distanceTo(nowX, nowY, map.minX, nowY) / FACTOR, 3);
		yforce += 5500 / Math.pow(Utils.distanceTo(nowX, nowY, nowX, map.maxY) / FACTOR, 3);
		yforce -= 5500 / Math.pow(Utils.distanceTo(nowX, nowY, nowX, map.minY) / FACTOR, 3);

		int lx = nowX - xforce;
		int ly = nowY - yforce;
		// x轴方向
		int angleX = Utils.angleTo(nowX, nowY, lx, nowY);
		// y轴方向
		int angleY = Utils.angleTo(nowX, nowY, nowX, ly);

		Position positionX = Utils.getNextTankPostion(nowX, nowY, angleX, 1);
		Position positionY = Utils.getNextTankPostion(nowX, nowY, angleY, 1);
		// x轴方向有block，不移动
		if (mDatabase.inBlocks(positionX.x, positionX.y) || mDatabase.isNearBorderCantMove(positionX.x, positionX.y)) {
			lx = nowX;
			ly -= yforce > 0 ? Math.abs(xforce) : -Math.abs(xforce);
		} else if (mDatabase.inBlocks(positionY.x, positionY.y)
				|| mDatabase.isNearBorderCantMove(positionY.x, positionY.y)) {
			ly = nowY;
			lx -= xforce > 0 ? Math.abs(yforce) : -Math.abs(yforce);
		}
		// Move in the direction of our resolved force.
		log.debug(String.format("[AntiGraveMove]tankid[%d]now[%d,%d]xforce[%d]yforce[%d]result[%d,%d]", tankid, nowX,
				nowY, xforce, yforce, lx, ly));
		goTo(lx, ly);
	}

	public boolean move(int tick) {
		try {
			mTick = tick;
			// 正在闪躲，不move
			if (mHelper.isDodgeing(mTick)) {
				return false;
			}
			// antiStupid(tick);
			createGravePoints();
			antiGraveMove();
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}
		return true;
	}

	/** Move towards an x and y coordinate **/
	public void goTo(int x, int y) {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int tankid = mDatabase.getMyTankId();
		int angle = Utils.angleTo(nowX, nowY, x, y);
		log.debug(String.format("[AntiGraveMove]tankid[%d]now[%d,%d]target[%d,%d]r[%d]", tankid, nowX, nowY, x, y,
				angle));
		mHelper.addMoveByTick(angle, 1);

	}

	protected void createGravePoints() {
		mGravePoints.clear();

		List<GravePoint> enemyPoints = createGravePointsByEnemys();
		List<GravePoint> friendPoints = createGravePointsByFriends();
		List<GravePoint> blockPoints = createGravePointsByBlocks();
		List<GravePoint> bulletPoints = createGravePointsByBulletsLines();
		//List<GravePoint> hitPoints = createGravePointsByHit();
		// List<GravePoint> randomPoints = createGravePointsByRandom();

		mGravePoints.addAll(enemyPoints);
		mGravePoints.addAll(friendPoints);
		mGravePoints.addAll(blockPoints);
		mGravePoints.addAll(bulletPoints);
		//mGravePoints.addAll(hitPoints);

	}

	/**
	 * 敌人为目标时，为引力 离近时为斥力 离远时为引力
	 * 
	 * @return
	 */
	protected List<GravePoint> createGravePointsByEnemys() {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int tankid = mDatabase.getMyTankId();
		int targetId = mAttackRadar.getTargetTankId();
		int bulletSize = mDatabase.getBullets().size();
		List<GravePoint> points = new ArrayList<>();
		List<TankGameInfo> list = mDatabase.getEnemyTanks();

		for (TankGameInfo tank : list) {
			boolean attackMe = mDatabase.isAttackMe(tank.id);
			int dis = Utils.distanceTo(nowX, nowY, tank.x, tank.y);
			// 射界被遮挡
			boolean fireBlock = mDatabase.fireInBlocks(nowX, nowY, tank.x, tank.y);
			boolean fireNearReady = mFireHelper.nearFire();
			int power = -300;
			if (tank.id == targetId) {
				if (fireBlock || dis > AppConfig.COMBAT_MAX_DISTANCE) {
					power = 200;
				} else if (fireNearReady) {
					power = -200;
				}
			}

			else if (bulletSize >= 2 && attackMe) {
				power = -1000;
			} else if (dis > AppConfig.COMBAT_MAX_DISTANCE) {
				power = 200;
			} else if (dis < AppConfig.COMBAT_MIN_DISTANCE) {
				power = -300;
			}
			GravePoint gPoint = new GravePoint(tank.x, tank.y, power);
			points.add(gPoint);
		}

		return points;
	}

	/**
	 * 创建障碍物斥力点
	 * 
	 * @return
	 */
	protected List<GravePoint> createGravePointsByBlocks() {
		// 每60个tick,block斥力增加1倍，持续30tick，把在死角的tank顶出去
		int power = BLOCK_POWER;
		if (mTick % 100 == 0) {
			power = BLOCK_POWER * 1;
		}
//		mBlockTimer++;
//		if (mBlockTimer < 3) {
//			power = BLOCK_POWER * 2;
//		}
		List<GravePoint> points = new ArrayList<>();
		List<TankMapBlock> blocks = mDatabase.getBlocks();
		for (TankMapBlock block : blocks) {
			GravePoint gPoint = new GravePoint(block.x, block.y, power);
			points.add(gPoint);
		}
		return points;
	}

	// 在中心一个随机点，每5回合随机斥力或引力
	protected List<GravePoint> createGravePointsByRandom() {
		List<GravePoint> points = new ArrayList<>();
		mTurn++;
		int power = 0;

		Position middlePosition = mDatabase.getMiddlePostion();
//		int[] array = { 45, 135, 225, 315 };
//		for (int i = 0; i < array.length; i++) {
//			int r = array[i];
//			Position pos = Utils.getNextPositionByDistance(middlePosition.x, middlePosition.y, r,
//					mDatabase.getBattleFieldWidth() / 4);
//			if (mTurn > 50) {
//				boolean b = mRandom.nextBoolean();
//				int seed = b ? 1 : -1;
//				power = seed * 500;
//			}
//			GravePoint point = new GravePoint(pos.x, pos.y, power);
//			points.add(point);
//		}

		if (mTurn > 50) {
			mTurn = 0;
			boolean b = mRandom.nextBoolean();
			int seed = b ? 1 : -1;
			power = seed * 1000;
		}
		GravePoint point = new GravePoint(middlePosition.x, middlePosition.y, power);
		points.add(point);
		return points;
	}

	protected List<GravePoint> createGravePointsByBulletsLines() {
		List<GravePoint> points = new ArrayList<>();
		List<Bullet> bullets = mDatabase.getBullets();
		for (Bullet bullet : bullets) {
			int x = bullet.currentX;
			int y = bullet.currentY;
			for (int i = 0; i < 5; i++) {
				Position position = Utils.getNextBulletByTick(x, y, bullet.r, i);
				GravePoint gPoint = new GravePoint(position.x, position.y, -150);
				points.add(gPoint);
			}
		}
		return points;
	}

	protected List<GravePoint> createGravePointsByBullets() {
		List<GravePoint> points = new ArrayList<>();
		List<Bullet> bullets = mDatabase.getBullets();
		int bulletSize = mDatabase.getBullets().size();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		for (Bullet bullet : bullets) {
			int power = -500;
			if (bulletSize >= 2) {
				power = -800;
			}

			GravePoint gPoint = new GravePoint(bullet.currentX, bullet.currentY, power);
			points.add(gPoint);
		}
		return points;
	}

	/**
	 * 创建友军引力点 大于最大距离 为引力 小于最小距离 为斥力
	 * 
	 * @return
	 */
	protected List<GravePoint> createGravePointsByFriends() {
		List<GravePoint> points = new ArrayList<>();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		List<TankGameInfo> friends = mDatabase.getFriendTanks();
		TankGameInfo leader = mDatabase.getLeader();
		for (TankGameInfo tank : friends) {
			int dis = Utils.distanceTo(nowX, nowY, tank.x, tank.y);
			int power = -500;
			if (tank.id == leader.id) {
				if (dis > AppConfig.COMBAT_MAX_DISTANCE) {
					power = 300;
				} else if (dis < AppConfig.COMBAT_MIN_DISTANCE) {
					power = -500;
				}
			}
			GravePoint gPoint = new GravePoint(tank.x, tank.y, power);
			points.add(gPoint);
		}
		return points;
	}

	/**
	 * 创建弹着点斥力
	 * 
	 * @return
	 */
	protected List<GravePoint> createGravePointsByHit() {
		List<GravePoint> points = new ArrayList<>();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		List<Bullet> bullets = mDatabase.getBullets();

		for (Bullet bullet : bullets) {
			int dis = Utils.distanceTo(nowX, nowY, bullet.currentX, bullet.currentY);
			Position position = getHitPosition(bullet, nowX, nowY);
			if (position != null) {
				GravePoint gPoint = new GravePoint(position.x, position.y, -400);
				points.add(gPoint);
			}
		}

		List<Position> crossPositions = getCrossHitPosition(bullets);
		for (Position position : crossPositions) {
			GravePoint gPoint = new GravePoint(position.x, position.y, -400);
			points.add(gPoint);
		}
		return points;
	}

	public Position getHitPosition(Bullet bullet, int nowX, int nowY) {
		HitResult result = new HitResult();
		result.willHitMe = false;
		// 計算圓心到彈道的垂足 ，p1,p2为弹道 p3圆心
		Position p1 = new Position(bullet.currentX, bullet.currentY);
		Position p2 = Utils.getNextBulletByTick(bullet.startX, bullet.startY, bullet.r, 2);
		Position p3 = new Position(nowX, nowY);
		// 垂足
		Position p4 = Utils.getFoot(p1, p2, p3);

		int angleHit = Utils.angleTo(bullet.startX, bullet.startY, p4.x, p4.y);
		// System.out.println("angleHit=" + angleHit);
		// 子弹已远离
		if (Math.abs(angleHit - bullet.r) >= 170) {
			return null;
		}
		// 垂足到圆心距离，即我与弹道垂足的位置， 半径减去此值就是最小移动距离
		int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);
		// System.out.println("a=" + a);
		// 小于半径会被击中
		if (a < AppConfig.TANK_WIDTH) {
			int b = (int) Math.sqrt(AppConfig.TANK_WIDTH * AppConfig.TANK_WIDTH - a * a);
			// 总距离，子弹到圆心的距离
			int totalDistance = Utils.distanceTo(p4.x, p4.y, p1.x, p1.y);
			// 子弹到我的距离，不是到圆心的距离，而是到弹着点的距离 =
			int distance = totalDistance - b;

			Position hitPosition = Utils.getNextPositionByDistance(bullet.currentX, bullet.currentY, bullet.createTick,
					distance);
			return hitPosition;
		}
		return null;
	}

	public List<Position> getCrossHitPosition(List<Bullet> bullets) {
		List<Position> list = new ArrayList<Position>();
		for (int i = 0; i < bullets.size(); i++) {
			if (i >= bullets.size() - 1) {
				break;
			}
			Bullet bullet1 = bullets.get(i);
			for (int j = i + 1; j < bullets.size(); j++) {
				Bullet bullet2 = bullets.get(j);
				Position position = Utils.crossPoint(new Position(bullet1.startX, bullet1.startY),
						new Position(bullet1.currentX, bullet1.currentY), new Position(bullet2.startX, bullet2.startY),
						new Position(bullet2.currentX, bullet2.currentY));
				if (position != null) {
					list.add(position);
				}
			}
		}
		return list;
	}

}
