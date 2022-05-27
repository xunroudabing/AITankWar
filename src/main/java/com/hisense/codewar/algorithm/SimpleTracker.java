package com.hisense.codewar.algorithm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.data.CombatMovementHelper;
import com.hisense.codewar.data.CombatMovementHelper.PollingAction;
import com.hisense.codewar.data.CombatMovementHelper.PostionEvent;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankMapBlock;
import com.hisense.codewar.utils.Utils;

public class SimpleTracker implements ITrackingAlgorithm {
	private CombatRealTimeDatabase mDatabase;
	private CombatMovementHelper mHelper;
	private static final Logger log = LoggerFactory.getLogger(SimpleTracker.class);

	public SimpleTracker(CombatRealTimeDatabase database, CombatMovementHelper helper) {
		// TODO Auto-generated constructor stub
		mDatabase = database;
		mHelper = helper;
	}

	public Position track(int nowX, int nowY, int targetX, int targetY, int tick) {
		// TODO Auto-generated method stub
		// 偶数处理x坐标 奇数处理y坐标
		int seed = tick / AppConfig.MOVE_CHASE_SPEED;
		boolean direction = seed % 2 == 0;
		log.debug("track + " + seed);
		Position position = null;
		int moveTick = 2;

		if (direction) {
			if (targetX > nowX) {
				Position p = Utils.getNextTankPostion(nowX, nowY, 0, moveTick);
				if (isValid(p)) {
					position = p;
				}
			} else {
				Position p = Utils.getNextTankPostion(nowX, nowY, 180, moveTick);
				if (isValid(p)) {
					position = p;
				}
			}

			if (position == null) {
				if (targetY > nowY) {
					Position p = Utils.getNextTankPostion(nowX, nowY, 90, moveTick);
					if (isValid(p)) {
						position = p;
					}
				} else {
					Position p = Utils.getNextTankPostion(nowX, nowY, 270, moveTick);
					if (isValid(p)) {
						position = p;
					}
				}
			}
		} else {
			if (targetY > nowY) {
				Position p = Utils.getNextTankPostion(nowX, nowY, 90, moveTick);
				if (isValid(p)) {
					position = p;
				}
			} else {
				Position p = Utils.getNextTankPostion(nowX, nowY, 270, moveTick);
				if (isValid(p)) {
					position = p;
				}
			}

			if (position == null) {
				if (targetX > nowX) {
					Position p = Utils.getNextTankPostion(nowX, nowY, 0, moveTick);
					if (isValid(p)) {
						position = p;
					}
				} else {
					Position p = Utils.getNextTankPostion(nowX, nowY, 180, moveTick);
					if (isValid(p)) {
						position = p;
					}
				}
			}
		}
		if (position != null) {
			int r = Utils.angleTo(nowX, nowY, position.x, position.y);
			mHelper.addMoveByTick(r, moveTick);
			//mHelper.addPollingEventByPos(PollingAction.CLOSETO, position.x, position.y, r, moveTick);
			log.debug("track angle is " + r);
		} else {
			log.debug("nULLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
		}
		return position;
	}

	public boolean inBlocks(Position position) {
		List<TankMapBlock> blocks = mDatabase.getBlocks();
		return Utils.inBlocks(position.x, position.y, AppConfig.BLOCK_SIZE, blocks, AppConfig.BLOCK_SIZE);
	}

	public boolean isValid(Position position) {
		List<TankMapBlock> blocks = mDatabase.getBlocks();
		boolean inBlock = Utils.inBlocks(position.x, position.y, AppConfig.BLOCK_SIZE, blocks, AppConfig.BLOCK_SIZE);
		boolean outOfRange = mDatabase.isOutRange(position.x, position.y);
		log.debug("inblock=" + inBlock + ",outof=" + outOfRange);
		return !inBlock && !outOfRange;
	}

	@Override
	public Position antitrack(int nowX, int nowY, int targetX, int targetY, int tick) {
		// TODO Auto-generated method stub
		// 偶数处理x坐标 奇数处理y坐标
		int seed = tick / AppConfig.MOVE_CHASE_SPEED;
		boolean direction = seed % 2 == 0;
		int moveTick = 2;
		Position position = null;
		if (direction) {
			if (targetX > nowX) {
				Position p = Utils.getNextTankPostion(nowX, nowY, 180, moveTick);
				if (isValid(p)) {
					position = p;
				}
			} else {
				Position p = Utils.getNextTankPostion(nowX, nowY, 0, moveTick);
				if (isValid(p)) {
					position = p;
				}
			}

			if (position == null) {
				if (targetY > nowY) {
					Position p = Utils.getNextTankPostion(nowX, nowY, 270, moveTick);
					if (isValid(p)) {
						position = p;
					}
				} else {
					Position p = Utils.getNextTankPostion(nowX, nowY, 90, moveTick);
					if (isValid(p)) {
						position = p;
					}
				}
			}
		} else {
			if (targetY > nowY) {
				Position p = Utils.getNextTankPostion(nowX, nowY, 270, moveTick);
				if (isValid(p)) {
					position = p;
				}
			} else {
				Position p = Utils.getNextTankPostion(nowX, nowY, 90, moveTick);
				if (isValid(p)) {
					position = p;
				}
			}

			if (position == null) {
				if (targetX > nowX) {
					Position p = Utils.getNextTankPostion(nowX, nowY, 180, moveTick);
					if (isValid(p)) {
						position = p;
					}
				} else {
					Position p = Utils.getNextTankPostion(nowX, nowY, 0, moveTick);
					if (isValid(p)) {
						position = p;
					}
				}
			}
		}
		if (position != null) {
			int r = Utils.angleTo(nowX, nowY, position.x, position.y);
			mHelper.addMoveByTick(r, moveTick);
			//mHelper.addPollingEventByPos(PollingAction.CLOSETO, position.x, position.y, r, moveTick);
		} else {
			log.debug("nULLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
		}
		return position;
	}
}
