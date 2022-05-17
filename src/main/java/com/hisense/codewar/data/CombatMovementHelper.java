package com.hisense.codewar.data;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGameActionType;

public class CombatMovementHelper {
	private MoveEvent mMoveEvent;
	private MoveEvent mDodgeEvent;
	private BlockingQueue<MoveEvent> mMoveQueue;
	private BlockingQueue<MoveEvent> mDodgeQueue;
	private static final Logger log = LoggerFactory.getLogger(CombatMovementHelper.class);

	public CombatMovementHelper() {
		mDodgeQueue = new LinkedBlockingQueue<>();
		mMoveQueue = new LinkedBlockingQueue<>();
	}

	public void reset() {
		mDodgeQueue.clear();
		mMoveQueue.clear();
		mMoveEvent = null;
		mDodgeEvent = null;
	}

	public boolean needDodge(int tick) {
		if (mDodgeEvent != null) {
			return true;
		} else if (!mDodgeQueue.isEmpty()) {
			return true;
		}
		return false;
	}

	public void addDodgeByTick(int r, int tick) {
		MoveEvent event = new MoveEvent();
		event.heading = r;
		event.tick = tick;
		mDodgeQueue.add(event);
	}

	public void addDodgeByDistance(int r, int distance) {
		MoveEvent event = new MoveEvent();
		event.heading = r;
		event.tick = distance / AppConfig.TANK_SPEED;
		mDodgeQueue.add(event);
	}
	
	public void addMoveByTick(int r, int tick) {
		MoveEvent event = new MoveEvent();
		event.heading = r;
		event.tick = tick;
		mMoveQueue.add(event);
	}

	public void addMoveByDistance(int r, int distance) {
		MoveEvent event = new MoveEvent();
		event.heading = r;
		event.tick = distance / AppConfig.TANK_SPEED;
		mMoveQueue.add(event);
	}

	public boolean dodge(ITtank tank, int tick) {
		if (mDodgeEvent == null) {
			try {
				mDodgeEvent = mDodgeQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.error(e.toString());
			}
		}

		if (mDodgeEvent != null) {
			int heading = mDodgeEvent.heading;
			int dodgeTick = mDodgeEvent.tick;
			if (dodgeTick <= 0) {
				log.debug(String.format("tank[%d]cant dodge,no tick left", tank.id));
				return false;
			}
			dodgeTick--;
			mDodgeEvent.tick = dodgeTick;
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
			log.debug(String.format("[Dodge]tank[%d]r[%d]tick[%d]", tank.id,heading,dodgeTick));
			if (dodgeTick == 0) {
				mDodgeEvent = null;
			}
			return true;
		}
		log.debug(String.format("tank[%d]cant dodge,no dodgeEvent", tank.id));
		return false;
	}

	public boolean move(ITtank tank,int tick) {
		if (mMoveEvent == null) {
			try {
				mMoveEvent = mMoveQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.error(e.toString());
			}
		}

		if (mMoveEvent != null) {
			int heading = mMoveEvent.heading;
			int moveTick = mMoveEvent.tick;
			if (moveTick <= 0) {
				log.debug(String.format("tank[%d]cant move,no tick left", tank.id));
				return false;
			}
			moveTick--;
			mMoveEvent.tick = moveTick;
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
			log.debug(String.format("[Move]tank[%d]r[%d]tick[%d]", tank.id,heading,moveTick));
			if (moveTick == 0) {
				mMoveEvent = null;
			}
			return true;
		}
		log.debug(String.format("tank[%d]cant move,no moveEvent", tank.id));
		return false;
	}

	public boolean canMove() {
		if (mMoveEvent != null) {
			return true;
		} else if (!mMoveQueue.isEmpty()) {
			return true;
		}
		return false;
	}

	public static class MoveEvent {
		public int heading;
		public int tick;
	}
}
