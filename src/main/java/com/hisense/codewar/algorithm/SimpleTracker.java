package com.hisense.codewar.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.ls.LSInput;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.data.CombatMovementHelper;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankMapBlock;
import com.hisense.codewar.utils.Utils;

public class SimpleTracker implements ITrackingAlgorithm {
	private CombatRealTimeDatabase mDatabase;
	private CombatMovementHelper mHelper;

	public SimpleTracker(CombatRealTimeDatabase database, CombatMovementHelper helper) {
		// TODO Auto-generated constructor stub
		mDatabase = database;
		mHelper = helper;
	}

	public List<Position> track(int nowX, int nowY, int targetX, int targetY, int tick) {
		// TODO Auto-generated method stub
		List<Position> list = new ArrayList<Position>();
		Position positionX = null;
		Position positionY = null;
		int xR = -1;
		int yR = -1;
		if (targetX > nowX) {
			Position position = Utils.getNextTankPostion(nowX, nowY, 0, 1);
			if (!inBlocks(position)) {
				positionX = position;
				xR = 0;
				list.add(positionX);
			}
		} else {
			Position position = Utils.getNextTankPostion(nowX, nowY, 180, 1);
			if (!inBlocks(position)) {
				positionX = position;
				xR = 180;
				list.add(positionX);
			}
		}

		if (targetY > nowY) {
			Position position = Utils.getNextTankPostion(nowX, nowY, 90, 1);
			if (!inBlocks(position)) {
				positionY = position;
				yR = 90;
				list.add(positionY);
				if (xR < 0) {
					Position positionYNext = Utils.getNextTankPostion(positionY.x, positionY.y, 90, 1);
					if (!inBlocks(positionYNext)) {
						list.add(positionYNext);
					}
				}
			}else {
				
			}
		} else {
			Position position = Utils.getNextTankPostion(nowX, nowY, 270, 1);
			if (!inBlocks(positionY)) {
				positionY = position;
				list.add(positionY);
				if (positionX == null) {
					Position positionYNext = Utils.getNextTankPostion(positionY.x, positionY.y, 270, 1);
					if (!inBlocks(positionYNext)) {
						list.add(positionYNext);
					}
				}
			}
		}

		if (targetX > nowX) {
			Position position = Utils.getNextTankPostion(nowX, nowY, 0, 1);
			if (!inBlocks(position)) {
				positionX = position;
				list.add(positionX);
			}
		} else {
			Position position = Utils.getNextTankPostion(nowX, nowY, 180, 1);
			if (!inBlocks(position)) {
				positionX = position;
				list.add(positionX);
			}
		}
		return list;
	}

	public boolean inBlocks(Position position) {
		List<TankMapBlock> blocks = mDatabase.getBlocks();
		return Utils.inBlocks(position.x, position.y, AppConfig.BLOCK_SIZE, blocks, AppConfig.BLOCK_SIZE);
	}

	@Override
	public List<Position> antitrack(int nowX, int nowY, int targetX, int targetY, int tick) {
		// TODO Auto-generated method stub
		return null;
	}
}
