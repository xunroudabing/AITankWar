package com.hisense.codewar.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGameActionType;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;

public class FireHelper {
	private int mTick = 30;
	CombatRealTimeDatabase mDatabase;
	CombatAttackRadar mAttackRadar;
	private static final Logger log = LoggerFactory.getLogger(FireHelper.class);

	public FireHelper(CombatRealTimeDatabase database, CombatAttackRadar attackRadar) {
		mDatabase = database;
		mAttackRadar = attackRadar;
	}

	public void reset() {
		mTick = 30;
	}

	// 装弹，满30或可以射击
	public void reload(int tick) {
		mTick++;
	}

	public boolean canFire() {
		return mTick >= 30;
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
		tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, dest);
		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		log.debug(String.format("[Fire]me[%d]pos[%d,%d]dest[%d]-->tankid[%d]pos[%d,%d]heading[%d]", mtankid, nowX, nowY,
				dest, target.id, target.x, target.y, target.r));
		mTick = 0;
		return true;
	}
}
