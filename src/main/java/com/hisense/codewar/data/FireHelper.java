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
import com.hisense.codewar.wave.WaveSurfing;

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

	public boolean nearFire() {
		return Math.abs(AppConfig.FIRE_SPAN - mTick) <= 20;
	}

	// 装弹，满30或可以射击
	public void reload(int tick) {
		mTick++;
	}

	public boolean canFire() {
		return mTick >= AppConfig.FIRE_SPAN;
	}

	public boolean wavefire(ITtank tank, WaveSurfing waveSurfing) {
		TankGameInfo target = mAttackRadar.getTargetTank();
		if (target == null) {
			log.debug("[Fire]not target!!");
			return false;
		}
		int mtankid = mDatabase.getMyTankId();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int heading = mDatabase.getHeading();

		int bestDest = Utils.angleTo(nowX, nowY, target.x, target.y);
		int range = Utils.getFireRange(nowX, nowY, target.x, target.y);
		// 波统计瞄准角度
		int aim = (int) waveSurfing.getBestMatchFireAngle(target.id);
		int dest = bestDest + aim;
		// 避免误伤
		if (willHitFriends(dest)) {
			return false;
		}
		// 射界内，不需要转向，直接开火
		if (Math.abs(dest - heading) <= range) {
			tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		} else {
			tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, dest);
		}

		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		log.debug(String.format("[WaveFire]me[%d]pos[%d,%d]dest[%d]bestDest[%d]aim[%d]-->tankid[%d]pos[%d,%d]heading[%d]",
				mtankid, nowX, nowY, dest, bestDest, aim, target.id, target.x, target.y, target.r));
		mTick = 0;
		return true;
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
			return false;
		}
		// 射界内，不需要转向，直接开火
		if (Math.abs(dest - heading) <= range) {
			tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		} else {
			// 射界内加随机数
			boolean b = mRandom.nextBoolean();
			int seed = b ? 1 : -1;
			int ranRange = mRandom.nextInt(range);
			dest = dest + seed * ranRange;
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
