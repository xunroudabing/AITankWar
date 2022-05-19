package com.hisense.codewar.data;

import java.util.Random;

import javax.xml.soap.MimeHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;

public class MoveMentRadar {
	private int mTick = 0;
	private Random mRandom = new Random();
	private CombatMovementHelper mHelper;
	private CombatAttackRadar mAttackRadar;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(MoveMentRadar.class);

	public MoveMentRadar(CombatRealTimeDatabase database, CombatAttackRadar radar, CombatMovementHelper helper) {
		mDatabase = database;
		mAttackRadar = radar;
		mHelper = helper;
	}

	public void reset() {
		mTick = 0;

	}

	public void scan(int tick) {
		mTick = tick;
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int heading = mDatabase.getHeading();

		TankGameInfo target = mAttackRadar.getTargetTank();
		if (target == null) {
			return;
		}

		int distance = Utils.distanceTo(nowX, nowY, target.x, target.y);
		// 拉近距离
		if (distance > AppConfig.COMBAT_DISTANCE) {
			int angle = Utils.angleTo(target.x, target.y, nowX, nowY);
			int fireRange = Utils.getFireRange(target.x, target.y, nowX, nowY);
			boolean b = mRandom.nextBoolean();
			int seed = b ? 1 : -1;
			// 避开射界
			int angleCantHit = angle + seed * (fireRange + 45);
			caculatePosByTarget(target, angleCantHit);

		} else if (distance >= 160 && distance <= AppConfig.COMBAT_DISTANCE) {
			// doNothing
		}
		// 拉开距离
		else if (distance < 160) {

		}
	}

	/**
	 * x1 = xcos(angle)-ysin(angle) y1 = xsin(angle)+ycos(angle)
	 * 
	 * @param target
	 * @param angle
	 */
	private void caculatePosByTarget(TankGameInfo target, int angle) {
		int dis = AppConfig.COMBAT_DISTANCE;
		int x = target.x;
		int y = target.y;

		int x1 = (int) (x * Math.cos(Utils.r2a(angle)) - y * Math.sin(Utils.r2a(angle)));
		int y1 = (int) (x * Math.cos(Utils.r2a(angle)) + y * Math.cos(Utils.r2a(angle)));
	}

}
