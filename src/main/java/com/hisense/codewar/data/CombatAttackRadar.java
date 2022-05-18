package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;

public class CombatAttackRadar {
	int mTick = 0;
	private int mTargetTankId;
	private TankGameInfo mTargetTank;

	private List<TankGameInfo> mTargets;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(CombatAttackRadar.class);

	public CombatAttackRadar(CombatRealTimeDatabase database) {
		mTargets = new ArrayList<TankGameInfo>();
		mDatabase = database;
	}

	public void reset() {
		mTargets.clear();
		mTick = 0;
		mTargetTankId = -1;
		mTargetTank = null;
	}

	public void scan(int tick) {
		mTick = tick;
		int mTankId = mDatabase.getMyTankId();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		List<TankGameInfo> friends = mDatabase.getFriendTanks();
		Iterator<TankGameInfo> iterator = mDatabase.getAllTanks().iterator();

		int targetTankId = -1;
		int minDistance = -1;
		while (iterator.hasNext()) {
			TankGameInfo tankGameInfo = (TankGameInfo) iterator.next();
			int tankid = tankGameInfo.id;
			if (mDatabase.isFriend(tankid)) {
				continue;
			} else if (tankid == mTankId) {
				continue;
			}
			int dis = caculateNearestTarget(new Position(nowX, nowY), new Position(tankGameInfo.x, tankGameInfo.y),
					friends);
			if (minDistance < 0) {
				minDistance = dis;
				targetTankId = tankGameInfo.getId();
			} else if (dis < minDistance) {
				minDistance = dis;
				targetTankId = tankGameInfo.getId();
			}
		}

		if (targetTankId != -1) {
			mTargetTankId = targetTankId;
		}

		TankGameInfo tank = mDatabase.getTankById(mTargetTankId);
		mTargetTank = new TankGameInfo(mTargetTankId, tank.x, tank.y, tank.r);
		log.debug(String.format("[T%d][AttackTarget]->%s", mTick,mTargetTank.toString()));
	}

	public TankGameInfo getTargetTank() {
		return mTargetTank;
	}

	public int getTargetTankId() {
		return mTargetTankId;
	}

	private int caculateNearestTarget(Position mPos, Position target, List<TankGameInfo> friends) {

		int mDis = Utils.distanceTo(mPos.x, mPos.y, target.x, target.y);
//		for (TankGameInfo info : friends) {
//			int dis = Utils.distanceTo(info.x, info.y, target.x, target.y);
//			mDis += dis;
//		}
		return mDis;
	}
}
