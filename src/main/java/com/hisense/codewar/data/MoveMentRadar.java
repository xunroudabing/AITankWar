package com.hisense.codewar.data;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.PoisionCircleUtils;
import com.hisense.codewar.utils.Utils;

public class MoveMentRadar {
	private int mTick = 0;
	private Random mRandom = new Random();
	private PoisionCircleUtils mPoisionCircleUtils;
	private CombatMovementHelper mHelper;
	private CombatAttackRadar mAttackRadar;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(MoveMentRadar.class);

	public MoveMentRadar(CombatRealTimeDatabase database, CombatAttackRadar radar, CombatMovementHelper helper,
			PoisionCircleUtils poisionCircleUtils) {
		mDatabase = database;
		mAttackRadar = radar;
		mHelper = helper;
		mPoisionCircleUtils = poisionCircleUtils;
	}

	public void reset() {
		mTick = 0;

	}

	public void scan(int tick) {
		mTick = tick;
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int heading = mDatabase.getHeading();

		avoidPoisionCircle(nowX, nowY, mTick);

		TankGameInfo target = mAttackRadar.getTargetTank();
		if (target == null) {
			return;
		}

		int distance = Utils.distanceTo(nowX, nowY, target.x, target.y);
		// 拉近距离
		if (distance > AppConfig.COMBAT_MAX_DISTANCE) {
			int angle = Utils.angleTo(target.x, target.y, nowX, nowY);
			int fireRange = Utils.getFireRange(target.x, target.y, nowX, nowY);
			boolean b = mRandom.nextBoolean();
			int seed = b ? 1 : -1;
			// 避开射界
			int angleCantHit = angle + seed * (fireRange + 45);
			// caculatePosByTarget(target);

		} else if (distance >= AppConfig.COMBAT_MIN_DISTANCE && distance <= AppConfig.COMBAT_MAX_DISTANCE) {
			// doNothing
		}
		// 拉开距离
		else if (distance < AppConfig.COMBAT_MIN_DISTANCE) {
			// caculatePosByTarget(target);

		}
	}

	/**
	 * x1 = xcos(angle)-ysin(angle) y1 = xsin(angle)+ycos(angle)
	 * 
	 * @param target
	 * @param angle
	 */
	private void caculatePosByTarget(TankGameInfo target) {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int mTankId = mDatabase.getMyTankId();
		if (mDatabase.isLeader()) {
			// 目标到我的角度
			int r = Utils.angleTo(target.x, target.y, nowX, nowY);
			// 目标为圆心，到我方向500米上的点
			Position moveToPosition = Utils.getNextPositionByDistance(nowX, nowY, r, AppConfig.COMBAT_CIRCLE_RADIUS);
			// 我距目標距離
			int distance = Utils.distanceTo(nowX, nowY, moveToPosition.x, moveToPosition.y);
			// 移动方向 todo 需要判斷障礙物
			int heading = Utils.angleTo(nowX, nowY, target.x, target.y);
			int needTick = Utils.getTicks(distance, AppConfig.TANK_SPEED);
			mHelper.addMoveByDistance(heading, distance);
			log.debug(String.format("[Move]leader[%d]head[%d]needtick[%d]distance[%d]-->enemy[%d]pos[%d,%d]", mTankId,
					heading, needTick, distance, target.id, target.x, target.y));
		} else {
			TankGameInfo leader = mDatabase.getLeader();
			// 目标与leader的角度
			int r = Utils.angleTo(target.x, target.y, leader.x, leader.y);
			// 偶数顺时针，奇数逆时针
			boolean clockWise = mTankId % 2 == 0;
			Position moveToPosition = getParterPosition(target, clockWise, r);
			if (moveToPosition != null) {
				// 我距目標距離
				int distance = Utils.distanceTo(nowX, nowY, moveToPosition.x, moveToPosition.y);
				// 移动方向 todo 需要判斷障礙物
				int heading = Utils.angleTo(nowX, nowY, moveToPosition.x, moveToPosition.y);
				mHelper.addMoveByDistance(heading, distance);
				int needTick = Utils.getTicks(distance, AppConfig.TANK_SPEED);
				log.debug(String.format("[Move]leader[%d]head[%d]needtick[%d]distance[%d]-->enemy[%d]pos[%d,%d]",
						mTankId, heading, needTick, distance, target.id, target.x, target.y));
			}

		}

	}

	private Position getParterPosition(TankGameInfo target, boolean clockwise, int r) {
		int angle = -1;
		int i = 0;
		while (i < 6) {
			int b = clockwise ? 1 : -1;
			angle = r + b * 60 * (i + 1);
			angle = Utils.formatAngle(angle);
			int x = target.x;
			int y = target.y;

			int x1 = (int) (x * Math.cos(Utils.r2a(angle)) - y * Math.sin(Utils.r2a(angle)));
			int y1 = (int) (x * Math.cos(Utils.r2a(angle)) + y * Math.cos(Utils.r2a(angle)));
			if (!Utils.isOutRange(x1, y1)) {
				return new Position(x1, y1);
			}
			i++;
		}
		return null;
	}

	private void avoidPoisionCircle(int x, int y, int tick) {

//		int speed = tick % 15;
//		if (speed != 0) {
//			return;
//		}
		int bulletCount = mDatabase.getThreatBulletsCount();
		if (bulletCount > 0) {
			log.debug("avoidPoisionCircle have " + bulletCount + " bullet wont move");
			return;
		}
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int tankid = mDatabase.getMyTankId();
		int heading = mDatabase.getHeading();
		if (mPoisionCircleUtils.inLeft(x, y)) {
			mHelper.addMoveByTick(0, 1);
		} else if (mPoisionCircleUtils.inRight(x, y)) {
			mHelper.addMoveByTick(180, 1);
		} else if (mPoisionCircleUtils.inTop(x, y)) {
			mHelper.addMoveByTick(270, 1);
		} else if (mPoisionCircleUtils.inBottom(x, y)) {
			mHelper.addMoveByTick(90, 1);
		} else {
			log.debug(String.format("[PoisionCircle]tank[%d]pos[%d,%d] do no nothing", tankid, nowX, nowY));
		}
	}

}
