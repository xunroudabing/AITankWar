package com.hisense.codewar.data;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private int mLastDodageTick = 0;
	private FireHelper mFireHelper;
	private CombatAttackRadar mAttackRadar;
	private CombatRealTimeDatabase mDatabase;
	private PostionEvent mMoveEvent;
	private MoveEvent mDodgeEvent;
	private PollingEvent mPollingEvent;
	private LimitedQueue<MoveEvent> mTrackQueue;
	private LimitedQueue<PollingEvent> mPollingQueue;
	private LimitedQueue<PostionEvent> mMoveQueue;
	private LimitedQueue<MoveEvent> mDodgeQueue;
	private static final int QUEUE_SIZE = 3;
	private static final Logger log = LoggerFactory.getLogger(CombatMovementHelper.class);

	public CombatMovementHelper(CombatRealTimeDatabase database, CombatAttackRadar radar, FireHelper fireHelper) {
		mDodgeQueue = new LimitedQueue<>(3);
		mMoveQueue = new LimitedQueue<>(QUEUE_SIZE);
		mPollingQueue = new LimitedQueue<>(QUEUE_SIZE);
		mTrackQueue = new LimitedQueue<>(QUEUE_SIZE);
		mDatabase = database;
		mAttackRadar = radar;
		mFireHelper = fireHelper;

	}

	public void reset() {
		mDodgeQueue.clear();
		mMoveQueue.clear();
		mPollingQueue.clear();
		mTrackQueue.clear();
		mMoveEvent = null;
		mDodgeEvent = null;
		mPollingEvent = null;
	}

	public boolean needTrack(int tick) {
		return !mTrackQueue.isEmpty();
	}

	public boolean needDodge(int tick) {
//		if (mDodgeEvent != null) {
//			int t = mDodgeEvent.tick;
//			return t > 0;
//		} else if (!mDodgeQueue.isEmpty()) {
//			return true;
//		}
//		return false;

		if (!mDodgeQueue.isEmpty()) {
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

	public void removeAllPolling() {
		mPollingQueue.clear();
		mPollingEvent = null;
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

	public void addDodgeByDistance(int startX, int startY, int dstX, int dstY, int r, int distance) {
		MoveEvent event = new MoveEvent();
		event.startX = startX;
		event.startY = startY;
		event.dstX = dstX;
		event.dstY = dstY;
		event.heading = r;
		BigDecimal b = BigDecimal.valueOf(distance);
		// distance / AppConfig.TANK_SPEED; CEILING，向上取整
		int tick = b.divide(BigDecimal.valueOf(AppConfig.TANK_SPEED), 0, BigDecimal.ROUND_CEILING).intValue();
		event.tick = tick;
//		int span = distance / 3;
//		if (span >= 3) {
//			mDodgeQueue.add(event);
//			mDodgeQueue.add(event);
//			mDodgeQueue.add(event);
//		} else if (span == 2) {
//			mDodgeQueue.add(event);
//			mDodgeQueue.add(event);
//		} else {
//			mDodgeQueue.add(event);
//		}
		mDodgeQueue.add(event);
	}

	public void removeAllMove() {
		mMoveQueue.clear();
		mMoveEvent = null;
	}

	public int getMoveToDoCount() {
		return mMoveQueue.size();
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

	public void addTrack(int r) {
		MoveEvent event = new MoveEvent();
		event.heading = r;
		mTrackQueue.add(event);
	}

	public boolean track(ITtank tank, int tick) {
		if (mTrackQueue.isEmpty()) {
			return false;
		}
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int heading = mDatabase.getHeading();
		for (int i = 0; i < 3; i++) {
			if (mTrackQueue.isEmpty()) {
				break;
			}
			MoveEvent moveEvent = mTrackQueue.poll();
			if (moveEvent != null) {
				tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, moveEvent.heading);
				log.debug(String.format("[Command-track][T%d]tank[%d]pos[%d,%d]chead[%d]r[%d]", tick, tank.id, nowX,
						nowY, heading, moveEvent.heading));
			} else {
				break;
			}
		}
		return true;
	}

	public boolean polling(ITtank tank, int tick) {
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
			if (i <= 0) {
				mPollingEvent = null;
			}

		}
		return false;
	}

	public void lock(ITtank tank, int tick) {
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

	public boolean isDodgeing(int tick) {
		if (!mDodgeQueue.isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean dodge2(ITtank tank, int tick) {
		return dodge2(tank, 3, tick);
	}

	public boolean dodge2(ITtank tank, int actionSize, int tick) {
		boolean firstRunning = false;
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int i = 0;
		while (!mDodgeQueue.isEmpty() && i < actionSize) {
			MoveEvent mDodgeEvent = mDodgeQueue.poll();
			if (mDodgeEvent != null) {
				int startX = mDodgeEvent.startX;
				int startY = mDodgeEvent.startY;
				int dx = mDodgeEvent.dstX;
				int dy = mDodgeEvent.dstY;
				int heading = mDodgeEvent.heading;
				int headingFix = Utils.angleTo(nowX, nowY, dx, dy);
				int dis = Utils.distanceTo(nowX, nowY, dx, dy);
				// 当前已经移动至目标点，考虑误差
				if (Utils.isNear(nowX, nowY, dx, dy)) {

					log.debug(String.format(
							"[Command-Dodge2-skip]tank[%d]currentPos[%d,%d]dstPos[%d,%d]chead[%d]r[%d]tick[%d]",
							tank.id, nowX, nowY, dx, dy, heading, heading, i));
					// 已到达指定位置
					break;
				} else {
					// 判断出界
					Position nextPosition = Utils.getNextPositionByDistance(nowX, nowY, heading,
							AppConfig.TANK_WIDTH * 2);
					if (mDatabase.isOut(nextPosition.x, nextPosition.y)) {
						// 出界，不执行此次命令
						log.debug(String.format(
								"[Command-Dodge2-Ignore][T%d]tank[%d]pos[%d,%d]r[%d]nextpos[%d,%d] will out of range",
								tick, tank.id, nowX, nowY, heading, nextPosition.x, nextPosition.y));
						break;
					}

					int span = dis / 3;
					if (span >= 3) {
						tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, headingFix);
						tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, headingFix);
						tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, headingFix);
					} else if (span == 2) {
						tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, headingFix);
						tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, headingFix);
					} else {
						tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, headingFix);
					}
					log.debug(String.format(
							"[Command-Dodge]tank[%d]startpos[%d,%d]pos[%d,%d]dstPos[%d,%d]errorDis[%d]heading[%d]headFix[%d]tick[%d]",
							tank.id, startX, startY, nowX, nowY, dx, dy, dis, heading, headingFix, span));

				}
			}
			i++;
		}
		mDodgeQueue.clear();
		return true;

	}

	public boolean dodge(ITtank tank, int tick) {
		boolean firstRunning = false;
		if (mDodgeEvent == null) {
			try {
				mDodgeEvent = mDodgeQueue.poll();
				firstRunning = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error(e.toString());
			}
		}

		if (mDodgeEvent != null) {
			int startX = mDodgeEvent.startX;
			int startY = mDodgeEvent.startY;
			int dx = mDodgeEvent.dstX;
			int dy = mDodgeEvent.dstY;
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

			int currentAngle = Utils.angleTo(nowX, nowY, dx, dy);
			// 调整与服务器端的坐标误差
//			if (Math.abs(currentAngle - heading) > 2) {
//				log.debug(String.format(
//						"[Command-Dodge-fixAngleError]tank[%d]currentPos[%d,%d]startPos[%d,%d]dstPos[%d,%d]currentAngle[%d]oriAngle[%d]",
//						tank.id, nowX, nowY, startX, startY, dx, dy, currentAngle, heading));
//				mDodgeEvent.heading = currentAngle;
//				heading = currentAngle;
//			}
			int currentHeading = mDatabase.getHeading();
			TankGameInfo enemyTank = mAttackRadar.getTargetTank();

			int dis = Utils.distanceTo(nowX, nowY, dx, dy);
			// 当前已经移动至目标点，考虑误差
			if (Utils.isNear(nowX, nowY, dx, dy)) {
				mDodgeEvent.tick = 0;
				mDodgeEvent = null;
				log.debug(String.format(
						"[Command-Dodge-skip]tank[%d]currentPos[%d,%d]dstPos[%d,%d]chead[%d]r[%d]tick[%d]", tank.id,
						nowX, nowY, dx, dy, currentHeading, heading, dodgeTick));
				// 已到达指定位置
				return false;
			}
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
			Position nextPosition = Utils.getNextPositionByDistance(nowX, nowY, heading, AppConfig.TANK_WIDTH * 2);
			if (mDatabase.isOut(nextPosition.x, nextPosition.y)) {
				// 出界，不执行此次命令
				log.debug(String.format(
						"[Command-Dodge-Ignore][T%d]tank[%d]pos[%d,%d]r[%d]nextpos[%d,%d] will out of range", tick,
						tank.id, nowX, nowY, heading, nextPosition.x, nextPosition.y));
				return false;
			}
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
			tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
			mLastDodageTick = tick;
			// 锁定
//			if (enemyTank != null) {
//				int dest = Utils.angleTo(nowX, nowY, enemyTank.x, enemyTank.y);
//				int range = Utils.getFireRange(nowX, nowY, enemyTank.x, enemyTank.y);
//				tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, dest);
//			} else {
//				tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, heading);
//			}

			log.debug(String.format(
					"[Command-Dodge]tank[%d]startpos[%d,%d]pos[%d,%d]dstPos[%d,%d]errorDis[%d]chead[%d]r[%d]tick[%d]",
					tank.id, startX, startY, nowX, nowY, dx, dy, dis, currentHeading, heading, dodgeTick));
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
		if (mMoveEvent == null) {
			try {
				mMoveEvent = mMoveQueue.poll();
			} catch (Exception e) {
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
			if (mMoveEvent.tick <= 0) {
				mMoveEvent = null;
			}
			int nowX = mDatabase.getNowX();
			int nowY = mDatabase.getNowY();

			// int dest = Utils.angleTo(nowX, nowY, x, y);
			// 判断出界
			// Position nextPosition = Utils.getNextTankPostion(nowX, nowY, dest, 2);
			Position nextPosition = Utils.getNextPositionByDistance(nowX, nowY, dest, AppConfig.TANK_WIDTH * 2);
			if (mDatabase.isOut(nextPosition.x, nextPosition.y)) {
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
			if (moveTick <= 0) {
				mMoveEvent = null;
			}
			log.debug(String.format("[T%d][Command-Move]tank[%d]pos[%d,%d]r[%d]nextPos[%d,%d]heading[%d]movetick[%d]",
					tick, tank.id, nowX, nowY, dest, x, y, dest, moveTick));
			return true;
		}
		log.debug(String.format("tank[%d]cant move,no moveEvent", tank.id));
		return false;
	}

	public boolean canPolling() {
		if (mPollingEvent != null) {
			return mPollingEvent.tick > 0;
		} else if (!mPollingQueue.isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean canMove() {
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
		if (mDatabase.inLeft(x, y)) {
			ret = 0;
		} else if (mDatabase.inRight(x, y)) {
			ret = 180;
		} else if (mDatabase.inTop(x, y)) {
			ret = 270;
		} else if (mDatabase.inBottom(x, y)) {
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
		public int dstX;
		public int dstY;
		public int startX;
		public int startY;
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
