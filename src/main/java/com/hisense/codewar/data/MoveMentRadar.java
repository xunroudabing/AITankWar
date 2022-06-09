package com.hisense.codewar.data;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.algorithm.ITrackingAlgorithm;
import com.hisense.codewar.algorithm.SimpleTracker;
import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.data.CombatMovementHelper.PollingAction;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.AStarUtil;
import com.hisense.codewar.utils.Node;
import com.hisense.codewar.utils.Utils;

public class MoveMentRadar {
	private int mTick = 0;
	private Random mRandom = new Random();
	private ITrackingAlgorithm mTrackerAI;
	private CombatMovementHelper mHelper;
	private CombatAttackRadar mAttackRadar;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(MoveMentRadar.class);

	public MoveMentRadar(CombatRealTimeDatabase database, CombatAttackRadar radar, CombatMovementHelper helper) {
		mDatabase = database;
		mAttackRadar = radar;
		mHelper = helper;
		mTrackerAI = new SimpleTracker(mDatabase, mHelper);
	}

	public void reset() {
		mTick = 0;

	}

	public void scan(int tick) {
		mTick = tick;
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int heading = mDatabase.getHeading();

		trackTarget();

//		int distance = Utils.distanceTo(nowX, nowY, target.x, target.y);
//		// 拉近距离
//		if (distance > AppConfig.COMBAT_MAX_DISTANCE) {
//			int angle = Utils.angleTo(target.x, target.y, nowX, nowY);
//			int fireRange = Utils.getFireRange(target.x, target.y, nowX, nowY);
//			boolean b = mRandom.nextBoolean();
//			int seed = b ? 1 : -1;
//			// 避开射界
//			int angleCantHit = angle + seed * (fireRange + 45);
//			// caculatePosByTarget(target);
//			
//
//		} else if (distance >= AppConfig.COMBAT_MIN_DISTANCE && distance <= AppConfig.COMBAT_MAX_DISTANCE) {
//			// doNothing
//		}
//		// 拉开距离
//		else if (distance < AppConfig.COMBAT_MIN_DISTANCE) {
//			// caculatePosByTarget(target);
//
//		}
	}

	// 拉近距离
	public void track(int nowX, int nowY, int targetX, int targetY, int tick) {

		mTrackerAI.track(nowX, nowY, targetX, targetY, tick);

	}

	// 拉远距离
	public void antitrack(int nowX, int nowY, int targetX, int targetY, int tick) {

		mTrackerAI.antitrack(nowX, nowY, targetX, targetY, tick);

	}

	protected void trackTarget() {
		int speed = mTick % AppConfig.MOVE_CHASE_SPEED;
		if (speed != 0) {
			return;
		}
		// log.debug("#########trackTarget############");
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int mTankId = mDatabase.getMyTankId();
		TankGameInfo target = null;
		if (mDatabase.isLeader()) {
			target = mAttackRadar.getTargetTank();
		} else {
			target = mDatabase.getLeader();
		}

//		if (target == null) {
//			target = mAttackRadar.getTargetTank();
//		}
		if (mTick > 600) {
			target = mAttackRadar.getTargetTank();
		}
		if (target == null) {
			return;
		}
		// 我距目標距離
		int distance = Utils.distanceTo(nowX, nowY, target.x, target.y);

		int bulletSize = mDatabase.getBullets().size();
		boolean underAttack = bulletSize > 0 ? true : false;
		boolean fireInBlock = mDatabase.fireInBlocks(nowX, nowY, target.x, target.y);

		if (underAttack) {
			return;
		}

		if (fireInBlock||distance > AppConfig.COMBAT_MAX_DISTANCE) {
			AStarUtil AHandler = new AStarUtil(mDatabase.getMapNodeArrays(), mDatabase.getPoisionR());
			Node startNode = new Node(nowX, nowY);
			TankGameInfo end = target;
			/*
			 * if(end.getAngelToMytank() < 300){ return; }
			 */
			if (end == null || (end.x == 0 && end.y == 0)) {
				//log.debug("activeAttack  没有目标坦克\n");
				return;
			}
			Node endNode = new Node(end.x, end.y);
			Node res_node = AHandler.astarSearch(startNode, endNode);
			if (res_node == null) {
				//log.debug("activeAttack  没有路径\n");
			} else {
				//log.debug("activeAttack 发现路径 \n");
			}

			Node lastNode = new Node(0, 0);
			while (res_node != null) {
				if (res_node.Father != null) {
					lastNode = res_node;
					res_node = res_node.Father;
				} else {
					break;
				}

			}
			if (res_node != null) {
				TankGameInfo tank = mDatabase.getTankById(mDatabase.getMyTankId());
				int ma = Utils.angleTo(nowX, nowY, lastNode.x, lastNode.y);
				for (int i = 0; i < 3; i++) {
					if (judgeTankMoveThreePointIsSafe(tank, ma, 1)) {
						mHelper.addTrack(ma);
					} else {
						break;
					}
				}
			}

		}

		// 目标到我的角度
//		int r = Utils.angleTo(nowX, nowY, target.x, target.y);
//		// 目标为圆心，到我方向200米上的点
//		// Position moveToPosition = Utils.getNextPositionByDistance(nowX, nowY, r,
//		// AppConfig.COMBAT_CIRCLE_RADIUS);
//
//		// 移动方向 todo 需要判斷障礙物
//		int heading = Utils.angleTo(nowX, nowY, target.x, target.y);
//		log.debug(String.format("*********distance[%d]r[%d]me[%d,%d]target[%d,%d]", distance, r, nowX, nowY, target.x,
//				target.y));
//		boolean fireBlock = false;
//		TankGameInfo enemyTank = mAttackRadar.getTargetTank();
//		if (enemyTank != null) {
//			fireBlock = mDatabase.fireInBlocks(nowX, nowY, enemyTank.x, enemyTank.y);
//		}
//		String des = mDatabase.isLeader() ? "leader" : "partner";
//		if (fireBlock) {
//			track(nowX, nowY, enemyTank.x, enemyTank.y, mTick);
//			log.debug(String.format("tankid[%d][%s]fireblock track to [%d,%d]", mTankId, des, target.x, target.y));
//		} else if (distance > AppConfig.COMBAT_MAX_DISTANCE) {
//			track(nowX, nowY, target.x, target.y, mTick);
//			log.debug(String.format("tankid[%d][%s]track to [%d,%d]", mTankId, des, target.x, target.y));
//		} else if (distance <= AppConfig.COMBAT_MIN_DISTANCE) {
//			antitrack(nowX, nowY, target.x, target.y, mTick);
//			log.debug(String.format("tankid[%d][%s]antitrack to [%d,%d]", mTankId, des, target.x, target.y));
//		}
	}

	public Boolean judgeTankMoveThreePointIsSafe(TankGameInfo tankInfo, double angel, int count) {
		if (count == 4) {
			return true;
		}
		TankGameInfo tmp = tankInfo;
		Position newPoint = Utils.getNextPositionByDistance(tmp.x, tmp.y, (int) angel, 3);
		// TankMapBlock newPoint =
		// TankGeometricOperationUtils.getEndPointByDisAndAngel((double)tmp.x,(double)tmp.y,(double)1,angel);
		tmp.x = newPoint.x;
		tmp.y = newPoint.y;
		int R = mDatabase.getPoisionR();

		if ((tmp.x > (800 + R - 20)) || (tmp.y > (0.5625 * (800 + R) - 20)) || (tmp.x < (800 - R + 20))
				|| (tmp.y < (0.5625 * (800 - R) + 20))) {
			//System.out.print("主动进攻路线规划  毒圈避开angel: " + angel + "\n");
			return false;
		}
		boolean inBlocks = mDatabase.inBlocks(tmp.x, tmp.y);
		if (inBlocks) {
			//System.out.print("主动进攻路线规划  block避开angel: " + angel + "\n");
			return false;
		}
		count++;
		return judgeTankMoveThreePointIsSafe(tmp, angel, count);
	}
	

	/**
	 * x1 = xcos(angle)-ysin(angle) y1 = xsin(angle)+ycos(angle)
	 * 
	 * @param target
	 * @param angle
	 */
	private void caculatePosByTarget(TankGameInfo target) {
//		int nowX = mDatabase.getNowX();
//		int nowY = mDatabase.getNowY();
//		int mTankId = mDatabase.getMyTankId();
//		if (mDatabase.isLeader()) {
//			// 目标到我的角度
//			int r = Utils.angleTo(target.x, target.y, nowX, nowY);
//			// 目标为圆心，到我方向500米上的点
//			Position moveToPosition = Utils.getNextPositionByDistance(nowX, nowY, r, AppConfig.COMBAT_CIRCLE_RADIUS);
//			// 我距目標距離
//			int distance = Utils.distanceTo(nowX, nowY, moveToPosition.x, moveToPosition.y);
//			// 移动方向 todo 需要判斷障礙物
//			int heading = Utils.angleTo(nowX, nowY, target.x, target.y);
//			int needTick = Utils.getTicks(distance, AppConfig.TANK_SPEED);
//			mHelper.addMoveByDistance(heading, distance);
//			log.debug(String.format("[Move]leader[%d]head[%d]needtick[%d]distance[%d]-->enemy[%d]pos[%d,%d]", mTankId,
//					heading, needTick, distance, target.id, target.x, target.y));
//		} else {
//			TankGameInfo leader = mDatabase.getLeader();
//			// 目标与leader的角度
//			int r = Utils.angleTo(target.x, target.y, leader.x, leader.y);
//			// 偶数顺时针，奇数逆时针
//			boolean clockWise = mTankId % 2 == 0;
//			Position moveToPosition = getParterPosition(target, clockWise, r);
//			if (moveToPosition != null) {
//				// 我距目標距離
//				int distance = Utils.distanceTo(nowX, nowY, moveToPosition.x, moveToPosition.y);
//				// 移动方向 todo 需要判斷障礙物
//				int heading = Utils.angleTo(nowX, nowY, moveToPosition.x, moveToPosition.y);
//				mHelper.addMoveByDistance(heading, distance);
//				int needTick = Utils.getTicks(distance, AppConfig.TANK_SPEED);
//				log.debug(String.format("[Move]leader[%d]head[%d]needtick[%d]distance[%d]-->enemy[%d]pos[%d,%d]",
//						mTankId, heading, needTick, distance, target.id, target.x, target.y));
//			}
//
//		}

	}

	private void handlePosition(int tick) {
		int speed = tick % 8;
		if (speed != 0) {
			return;
		}
		int bulletCount = mDatabase.getThreatBulletsCount();
		if (bulletCount > 0) {
			log.debug("avoidPoisionCircle have " + bulletCount + " bullet wont move");
			return;
		}
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		boolean isOut = mDatabase.isOut(nowX, nowY);
		if (isOut) {
			mHelper.removeAllPolling();
			mHelper.addPollingEventByAction(PollingAction.AVOID_POISION, 3);
		} else {
			farFromFriends();
		}
	}

	private void farFromFriends() {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int tankId = mDatabase.getMyTankId();
		List<TankGameInfo> friendTanks = mDatabase.getFriendTanks();
		for (TankGameInfo tank : friendTanks) {
			int distance = Utils.distanceTo(nowX, nowY, tank.x, tank.y);
			// 离得太近，拉开距离
			if (distance < AppConfig.COMBAT_MIN_DISTANCE) {
				antitrack(nowX, nowY, tank.x, tank.y, mTick);
				log.debug(
						String.format("tankid[%d]pos[%d,%d]antitrack to [%d,%d]", tankId, nowX, nowY, tank.x, tank.y));
				return;
			}
		}
	}

	private void avoidPoisionCircle(int x, int y, int tick) {

	}

}
