package com.hisense.codewar.data;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.algorithm.ITrackingAlgorithm;
import com.hisense.codewar.algorithm.SimpleTracker;
import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameActionType;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;

public class CombatMovementHelper {

	public static void main(String[] args) {
		BigDecimal b = BigDecimal.valueOf(28);
		// distance / AppConfig.TANK_SPEED; CEILING，向上取整
		int tick = b.divide(BigDecimal.valueOf(AppConfig.TANK_SPEED), 0, BigDecimal.ROUND_CEILING).intValue();
		System.out.println(tick);
	}

	private int mTick = 0;
	// 记录上次躲闪时间，躲闪5个tick内不做move动作
	private int mLastDodageTick = 0;
	private FireHelper mFireHelper;
	private CombatAttackRadar mAttackRadar;
	private CombatRealTimeDatabase mDatabase;
	private PostionEvent mMoveEvent;
	private MoveEvent mDodgeEvent;
	private PollingEvent mPollingEvent;
	private BlockingQueue<PollingEvent> mPollingQueue;
	private BlockingQueue<PostionEvent> mMoveQueue;
	private BlockingQueue<MoveEvent> mDodgeQueue;
	private static final int WONT_MOVE_DODAGEING = 5;
	private static final Logger log = LoggerFactory.getLogger(CombatMovementHelper.class);

	public CombatMovementHelper(CombatRealTimeDatabase database, CombatAttackRadar radar, FireHelper fireHelper) {
		mDodgeQueue = new LinkedBlockingQueue<>();
		mMoveQueue = new LinkedBlockingQueue<>();
		mPollingQueue = new LinkedBlockingQueue<>();
		mDatabase = database;
		mAttackRadar = radar;
		mFireHelper = fireHelper;

	}

	public void reset() {
		mDodgeQueue.clear();
		mMoveQueue.clear();
		mPollingQueue.clear();
		mMoveEvent = null;
		mDodgeEvent = null;
		mPollingEvent = null;
		mTick = 0;
		mLastDodageTick = 0;
	}

	public boolean needDodge(int tick) {
		if (mDodgeEvent != null) {
			int t = mDodgeEvent.tick;
			return t > 0;
		} else if (!mDodgeQueue.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * 返回当前躲闪耗时
	 * 
	 * @return
	 */
	public int getCurrentDodgeCost() {
		int cost = 0;
		if (mDodgeEvent != null) {
			cost += mDodgeEvent.tick;
		} else if (!mDodgeQueue.isEmpty()) {
			MoveEvent event = mDodgeQueue.peek();
			cost += event.tick;
		}
		return cost;
	}

	public void addPollingEventByAction(int action, int tick) {
		PollingEvent event = new PollingEvent();
		event.action = action;
		event.tick = tick;
		mPollingQueue.add(event);
	}

	public void addPollingEventByPos(int action, int x, int y, int r, int tick) {
		PollingEvent event = new PollingEvent();
		event.action = action;
		event.tx = x;
		event.ty = y;
		event.r = r;
		event.tick = tick;
		mPollingQueue.add(event);
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
		BigDecimal b = BigDecimal.valueOf(distance);
		// distance / AppConfig.TANK_SPEED; CEILING，向上取整
		int tick = b.divide(BigDecimal.valueOf(AppConfig.TANK_SPEED), 0, BigDecimal.ROUND_CEILING).intValue();
		event.tick = tick;
		mDodgeQueue.add(event);
	}

	public void addMoveByTick(int r, int tick) {
//		MoveEvent event = new MoveEvent();
//		event.heading = r;
//		event.tick = tick;
//		mMoveQueue.add(event);
		PostionEvent event = new PostionEvent();
		event.heading = r;
		event.tick = tick;
		mMoveQueue.add(event);
	}

	public void addMoveByDistance(int r, int distance) {
		PostionEvent event = new PostionEvent();
		event.heading = r;
		event.tick = Utils.getTicks(distance, AppConfig.TANK_SPEED);
		mMoveQueue.add(event);
	}

	public boolean polling(ITtank tank, int tick) {
		mTick = tick;
		// 控制速度
		if (tick % AppConfig.MOVE_ESCAPE_SPEED != 0) {
			return false;
		}
		log.debug("#####Polling time########");
		if (mPollingEvent == null) {
			try {
				mPollingEvent = mPollingQueue.take();
			} catch (Exception e) {
				// TODO: handle exception
				log.error(e.toString());
			}
		}

		if (mPollingEvent != null) {
			int i = mPollingEvent.tick;
			if (i <= 0) {
				mPollingEvent = null;
				log.debug(String.format("[T%d]tank[%d]cant polling,no tick left", tick, tank.id));
				return false;
			}
			log.debug(String.format("[Command-Polling]action[%d]r[%d]xy[%d,%d]tick[%d]", mPollingEvent.action,
					mPollingEvent.r, mPollingEvent.tx, mPollingEvent.ty, mPollingEvent.tick));
			i--;
			mPollingEvent.tick = i;
			switch (mPollingEvent.action) {
			case PollingAction.AVOID_POISION:
				if (mPollingEvent.tick <= 0) {
					mPollingEvent = null;
				}
				return avoidPoision(tank, tick);
			case PollingAction.CLOSETO:
			case PollingAction.FARFROM:
				tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, mPollingEvent.r);
				tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, mPollingEvent.r);
				tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, mPollingEvent.r);
				if (mPollingEvent.tick <= 0) {
					mPollingEvent = null;
				}
				return true;
			default:
				break;
			}

		}
		return false;
	}

	public void lock(ITtank tank, int tick) {
		mTick = tick;
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int currentHeading = mDatabase.getHeading();
		TankGameInfo enemyTank = mAttackRadar.getTargetTank();
		if (enemyTank != null) {
			int dest = Utils.angleTo(nowX, nowY, enemyTank.x, enemyTank.y);
			tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, dest);
			tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, dest);
			tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, dest);
		}
	}

	public boolean dodge(ITtank tank, int tick) {
		mTick = tick;
		mLastDodageTick = tick;
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
				log.debug(String.format("[T%d]tank[%d]cant dodge,no tick left", tick, tank.id));
				return false;
			}
			dodgeTick--;
			mDodgeEvent.tick = dodgeTick;
			int nowX = mDatabase.getNowX();
			int nowY = mDatabase.getNowY();
			int currentHeading = mDatabase.getHeading();
			TankGameInfo enemyTank = mAttackRadar.getTargetTank();

			// 先开火再闪避
//			boolean canFire = mFireHelper.canFire();
//			if (canFire) {
//				tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
//				mFireHelper.fireAndDodage(tank);
//				return true;
//			} else {
//				tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
//				tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
//			}
			// 判断出界
			Position nextPosition = Utils.getNextTankPostion(nowX, nowY, heading, 2);
			if (mDatabase.isOutRange(nextPosition.x, nextPosition.y)) {
				// 出界，不执行此次命令
				log.debug(String.format(
						"[Command-Dodge-Ignore][T%d]tank[%d]pos[%d,%d]r[%d]nextpos[%d,%d] will out of range", tick,
						tank.id, nowX, nowY, heading, nextPosition.x, nextPosition.y));
				return false;
			}
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);

			// 锁定
//			if (enemyTank != null) {
//				int dest = Utils.angleTo(nowX, nowY, enemyTank.x, enemyTank.y);
//				int range = Utils.getFireRange(nowX, nowY, enemyTank.x, enemyTank.y);
//				tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, dest);
//			} else {
//				tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
//			}

			log.debug(String.format("[Command-Dodge]tank[%d]pos[%d,%d]chead[%d]r[%d]tick[%d]", tank.id, nowX, nowY,
					currentHeading, heading, dodgeTick));
			if (dodgeTick == 0) {
				mDodgeEvent = null;
			}
			return true;
		}
		log.debug(String.format("[T%d]tank[%d]cant dodge,no dodgeEvent", tick, tank.id));
		return false;
	}

	// 停止移动
	public void stop(ITtank tank) {
		mMoveEvent = null;
		mMoveQueue.clear();
	}

	// 移动
	public boolean move(ITtank tank, int tick) {
		mTick = tick;
		if (mMoveEvent == null) {
			try {
				mMoveEvent = mMoveQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.error(e.toString());
			}
		}

		if (mMoveEvent != null) {

			int x = mMoveEvent.x;
			int y = mMoveEvent.y;
			int dest = mMoveEvent.heading;
			int moveTick = mMoveEvent.tick;
			int speed = mMoveEvent.speed;
			if (moveTick <= 0) {
				log.debug(String.format("[T%d]tank[%d]cant move,no tick left", tick, tank.id));
				mMoveEvent = null;
				return false;
			}
//			else if (tick % speed != 0) {
//				return false;
//			}
			moveTick--;
			mMoveEvent.tick = moveTick;
			int nowX = mDatabase.getNowX();
			int nowY = mDatabase.getNowY();

			// int dest = Utils.angleTo(nowX, nowY, x, y);
			// 判断出界
			Position nextPosition = Utils.getNextTankPostion(nowX, nowY, dest, 2);
			if (mDatabase.isOutRange(nextPosition.x, nextPosition.y)) {
				// 出界，不执行此次命令
				log.debug(String.format(
						"[Command-Move-Ignore][T%d]tank[%d]pos[%d,%d]r[%d]nextpos[%d,%d] will out of range", tick,
						tank.id, nowX, nowY, dest, nextPosition.x, nextPosition.y));
				return false;
			}
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, dest);
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, dest);

			TankGameInfo enemyTank = mAttackRadar.getTargetTank();
			if (enemyTank != null) {
				int angle = Utils.angleTo(nowX, nowY, enemyTank.x, enemyTank.y);
				tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, angle);
			}
			log.debug(String.format("[T%d][Command-Move]tank[%d]pos[%d,%d]r[%d]nextPos[%d,%d]heading[%d]movetick[%d]",
					tick, tank.id, nowX, nowY, dest, x, y, dest, moveTick));
			if (mMoveEvent.tick <= 0) {
				mMoveEvent = null;
			}
			return true;
		}
		log.debug(String.format("tank[%d]cant move,no moveEvent", tank.id));
		return false;
	}

	public boolean canPolling(int tick) {
		mTick = tick;
		// Dodage后的5tick后才可以做动作
		if (mTick - mLastDodageTick <= WONT_MOVE_DODAGEING) {
			return false;
		}
		if (mPollingEvent != null) {
			return mPollingEvent.tick > 0;
		} else if (!mPollingQueue.isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean canMove(int tick) {
		mTick = tick;
		// Dodage后的5tick后才可以做动作
		if (mTick - mLastDodageTick <= WONT_MOVE_DODAGEING) {
			return false;
		}
		if (mMoveEvent != null) {
			return mMoveEvent.tick > 0;
		} else if (!mMoveQueue.isEmpty()) {
			return true;
		}
		return false;
	}

	protected boolean avoidPoision(ITtank tank, int tick) {

		int ret = -1;
		int tankid = mDatabase.getMyTankId();
		int x = mDatabase.getNowX();
		int y = mDatabase.getNowY();
		if (mDatabase.getPoisionCircle().inLeft(x, y)) {
			ret = 0;
		} else if (mDatabase.getPoisionCircle().inRight(x, y)) {
			ret = 180;
		} else if (mDatabase.getPoisionCircle().inTop(x, y)) {
			ret = 270;
		} else if (mDatabase.getPoisionCircle().inBottom(x, y)) {
			ret = 90;
		} else {
			log.debug(String.format("[PoisionCircle]tank[%d]pos[%d,%d] do no nothing", tankid, x, y));
			return false;
		}

		tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, ret);
		tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, ret);
		tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, ret);

		return true;
	}

	public static class MoveEvent {
		public int heading;
		public int tick;
	}

	public static class PostionEvent {
		public int x;
		public int y;
		public int heading;
		public int tick;
		public int speed;

	}

	public static class PollingEvent {
		/**
		 * 1-脱离毒圈 2-拉近距离 3-拉远距离
		 */
		public int action;
		public int tick;
		public int tx;
		public int ty;
		public int r;
	}

	public static class PollingAction {
		public static final int AVOID_POISION = 1;
		public static final int CLOSETO = 2;
		public static final int FARFROM = 3;
	}
}
