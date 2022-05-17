package com.hisense.codewar.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGameActionType;

public class CombatMovementHelper {
	private int mHeading = 0;
	private int mTick = 0;
	private static final Logger log = LoggerFactory.getLogger(CombatMovementHelper.class);

	public CombatMovementHelper() {

	}
	public int getHeading() {
		return mHeading;
	}
	public void addMoveByTick(int r, int tick) {
		mHeading = r;
		mTick = tick;
	}

	public void addMoveByDistance(int r, int distance) {
		mHeading = r;
		mTick = distance / AppConfig.TANK_SPEED;
	}

	public boolean move(ITtank tank) {
		if (mTick <= 0) {
			log.debug(String.format("tank[%d]cant move,no tick left", tank.id));
			return false;
		}
		tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, mHeading);
		tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, mHeading);
		tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, mHeading);
		mTick--;
		return true;
	}
	
	public boolean canMove() {
		return mTick > 0;
	}
}
