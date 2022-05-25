package com.hisense.codewar.data;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.FireRange;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGameActionType;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;

public class FireHelper {
	private Random mRandom = new Random();
	private int mTick = 60;
	CombatRealTimeDatabase mDatabase;
	CombatAttackRadar mAttackRadar;
	private static final Logger log = LoggerFactory.getLogger(FireHelper.class);

	public FireHelper(CombatRealTimeDatabase database, CombatAttackRadar attackRadar) {
		mDatabase = database;
		mAttackRadar = attackRadar;
		mTick = AppConfig.FIRE_SPAN;
	}

	public void reset() {
		mTick = AppConfig.FIRE_SPAN;
	}

	// 装弹，满30或可以射击
	public void reload(int tick) {
		mTick++;
	}

	public boolean canFire() {
		return mTick >= AppConfig.FIRE_SPAN;
	}
	
	public boolean fire(ITtank tank) {

		TankGameInfo target = mAttackRadar.getTargetTank();
		if (target == null) {
			log.debug("[Fire]not target!!");
			return false;
		}
		int mtankid = mDatabase.getMyTankId();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int heading = mDatabase.getHeading();

		int dest = Utils.angleTo(nowX, nowY, target.x, target.y);
		int range = Utils.getFireRange(nowX, nowY, target.x, target.y);
		int distance = Utils.distanceTo(nowX, nowY, target.x, target.y);
		// 避免误伤
		if (willHitFriends(dest)) {
			// boolean b = mRandom.nextBoolean();
			return false;
		}
		// 射界内，不需要转向，直接开火
		if (Math.abs(dest - heading) <= range) {
			tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		} else {
			// 射界内加随机数
			if (distance > 500) {
				boolean b = mRandom.nextBoolean();
				int seed = b ? 1 : -1;
				int ranRange = mRandom.nextInt(range + 30);
				dest = dest + seed * ranRange;
			} else if (distance > 400 && distance <= 500) {
				boolean b = mRandom.nextBoolean();
				int seed = b ? 1 : -1;
				int ranRange = mRandom.nextInt(range + 20);
				dest = dest + seed * ranRange;
			} else if (distance > 300 && distance <= 400) {
//				boolean b = mRandom.nextBoolean();
//				int seed = b ? 1 : -1;
//				int ranRange = mRandom.nextInt(range + 10);
//				dest = dest + seed * ranRange;
			} else {
				// 射界内加随机数
				boolean b = mRandom.nextBoolean();
				int seed = b ? 1 : -1;
				int ranRange = mRandom.nextInt(range);
				dest = dest + seed * ranRange;
			}
			tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, dest);
		}

		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		log.debug(String.format("[Fire]me[%d]pos[%d,%d]dest[%d]-->tankid[%d]pos[%d,%d]heading[%d]", mtankid, nowX, nowY,
				dest, target.id, target.x, target.y, target.r));
		mTick = 0;
		return true;
	}

	public boolean willHitFriends(int dest) {
		boolean ret = false;
		List<FireRange> friends = mAttackRadar.getFriendsFireRange();
		for (FireRange fireRange : friends) {
			// 在友军射界内
			if (Math.abs(dest - fireRange.fireAngle) < fireRange.range) {
				ret = true;
				break;
			}
		}
		return ret;
	}

}
