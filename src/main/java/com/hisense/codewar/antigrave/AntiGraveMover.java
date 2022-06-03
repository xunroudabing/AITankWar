package com.hisense.codewar.antigrave;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		int px = 244;
		int py = 198;
		int power = -5;

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
	private static final int FACTOR = 20;
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
		xforce += 7500 / Math.pow(Utils.distanceTo(nowX, nowY, map.maxX, nowY) / FACTOR, 3);
		xforce -= 7500 / Math.pow(Utils.distanceTo(nowX, nowY, map.minX, nowY) / FACTOR, 3);
		yforce += 7500 / Math.pow(Utils.distanceTo(nowX, nowY, nowX, map.maxY) / FACTOR, 3);
		yforce -= 7500 / Math.pow(Utils.distanceTo(nowX, nowY, nowX, map.minY) / FACTOR, 3);

		int lx = nowX - xforce;
		int ly = nowY - yforce;
		// x轴方向
		int angleX = Utils.angleTo(nowX, nowY, lx, nowY);
		// y轴方向
		int angleY = Utils.angleTo(nowX, nowY, nowX, ly);

		Position positionX = Utils.getNextTankPostion(nowX, nowY, angleX, 1);
		Position positionY = Utils.getNextTankPostion(nowX, nowY, angleX, 1);
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

	public void move(int tick) {
		try {
			mTick = tick;
			// 正在闪躲，不move
			if (mHelper.isDodgeing(mTick)) {
				return;
			}
			// antiStupid(tick);
			createGravePoints();
			antiGraveMove();
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}
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

	protected void antiStupid(int tick) {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int tankid = mDatabase.getMyTankId();
		if (mLastPosition == null) {
			mLastPosition = new Position(nowX, nowY);
		} else {
			int dis = Math.abs(nowX - mLastPosition.x);
			mLastPosition = new Position(nowX, nowY);
			if (dis < 100) {
				stupidTimer++;
				if (stupidTimer > 300) {
					isStupid = true;
					log.debug(String.format("[AntiGrave-AntiStupid]tankid[%d]stupidPos[%d,%d]", tankid, nowX, nowY));
				}
			} else {
				isStupid = false;
				stupidTimer = 0;
			}
		}
	}

	protected void createGravePoints() {
		mGravePoints.clear();

		List<GravePoint> enemyPoints = createGravePointsByEnemys();
		List<GravePoint> friendPoints = createGravePointsByFriends();
		List<GravePoint> blockPoints = createGravePointsByBlocks();
		List<GravePoint> bulletPoints = createGravePointsByBullets();
		List<GravePoint> randomPoints = createGravePointsByRandom();
		// List<GravePoint> stupidPoints = createStupidPoints();

		mGravePoints.addAll(enemyPoints);
		mGravePoints.addAll(friendPoints);
		mGravePoints.addAll(blockPoints);
		mGravePoints.addAll(bulletPoints);
		mGravePoints.addAll(randomPoints);
		// mGravePoints.addAll(stupidPoints);

	}

	protected List<GravePoint> createStupidPoints() {
		if (isStupid) {
			int nowX = mDatabase.getNowX();
			int nowY = mDatabase.getNowY();

			int[] array = { 0, 90, 180, 270 };
			Position stupidPos = null;
			int dis = mDatabase.getBattleFieldWidth() / 4;
			for (int i = 0; i < array.length; i++) {
				Position position = Utils.getNextPositionByDistance(nowX, nowY, array[i], 150);
				boolean inBlock = mDatabase.inBlocks(position.x, position.y);
				boolean isOut = mDatabase.isOut(position.x, position.y);
				if (!inBlock && !isOut) {
					stupidPos = position;
					break;
				}
			}
			if (stupidPos == null) {
				return mStupidPoints;
			}
//			if (mStupidPoints.size() > 3) {
//				return mStupidPoints;
//			}
			GravePoint point = new GravePoint(stupidPos.x, stupidPos.y, 500);
			mStupidPoints.add(point);
		} else {
			mStupidPoints.clear();
		}

		return mStupidPoints;
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
		List<GravePoint> points = new ArrayList<>();
		List<TankGameInfo> list = mDatabase.getEnemyTanks();

		for (TankGameInfo tank : list) {

			int dis = Utils.distanceTo(nowX, nowY, tank.x, tank.y);
			// 射界被遮挡
			boolean fireBlock = mDatabase.fireInBlocks(nowX, nowY, tank.x, tank.y);
			boolean fireNearReady = mFireHelper.nearFire();
			int power = -500;
			if (tank.id == targetId) {
				if (fireBlock || dis > AppConfig.COMBAT_MAX_DISTANCE) {
					power = 1000;
				} else if (fireNearReady) {
					power = 200;
				}
			} else if (dis > AppConfig.COMBAT_MAX_DISTANCE) {
				power = 1000;
			} else if (dis < AppConfig.COMBAT_MIN_DISTANCE) {
				power = -500;
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
		int[] array = { 45, 135, 225, 315 };
		for (int i = 0; i < array.length; i++) {
			int r = array[i];
			Position pos = Utils.getNextPositionByDistance(middlePosition.x, middlePosition.y, r,
					mDatabase.getBattleFieldWidth() / 4);
			if (mTurn > 50) {
				boolean b = mRandom.nextBoolean();
				int seed = b ? 1 : -1;
				power = seed * 500;
			}
			GravePoint point = new GravePoint(pos.x, pos.y, power);
			points.add(point);
		}

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

	protected List<GravePoint> createGravePointsByBullets() {
		List<GravePoint> points = new ArrayList<>();
		List<Bullet> bullets = mDatabase.getBullets();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		for (Bullet bullet : bullets) {
			int power = -200;
			int dis = Utils.distanceTo(nowX, nowY, bullet.currentX, bullet.currentY);
			if (dis > 0 && dis < 100) {
				power = -1000;
			} else if (dis > 100 && dis < 200) {
				power = -500;
			} else if (dis > 200 && dis < 300) {
				power = -300;
			} else {

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
			int power = -700;
			if (tank.id == leader.id) {
				if (dis > AppConfig.COMBAT_MAX_DISTANCE) {
					power = 1000;
				} else if (dis < AppConfig.COMBAT_MIN_DISTANCE) {
					power = -700;
				}
			} 
//			else {
//				if (dis > AppConfig.COMBAT_MAX_DISTANCE) {
//					power = 1000;
//				}
//			}
			GravePoint gPoint = new GravePoint(tank.x, tank.y, power);
			points.add(gPoint);
		}
		return points;
	}

}
