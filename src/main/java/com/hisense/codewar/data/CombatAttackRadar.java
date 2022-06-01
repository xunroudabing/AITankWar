package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.model.FireRange;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;

public class CombatAttackRadar {
	int mTick = 0;
	private int mNearestTankId = -1;
	private int mTargetTankId = -1;
	private TankGameInfo mTargetTank;
	private List<TankGameInfo> mEnemyCanFire;
	private List<FireRange> mFriendsFireRange;
	private List<TankGameInfo> mTargets;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(CombatAttackRadar.class);

	public CombatAttackRadar(CombatRealTimeDatabase database) {
		mTargets = new ArrayList<TankGameInfo>();
		mFriendsFireRange = new ArrayList<>();
		mEnemyCanFire = new ArrayList<TankGameInfo>();
		mDatabase = database;
	}

	public void reset() {
		mEnemyCanFire.clear();
		mFriendsFireRange.clear();
		mTargets.clear();
		mTick = 0;
		mTargetTankId = -1;
		mNearestTankId = -1;
		mTargetTank = null;
	}

	// 计算友军射界，避免误伤
	private void caculateFriendsFireRange(int nowX, int nowY, TankGameInfo tank) {
		int fireAngle = Utils.angleTo(nowX, nowY, tank.x, tank.y);
		int range = Utils.getFireRange(nowX, nowY, tank.x, tank.y);
		FireRange fireRange = new FireRange();
		fireRange.fireAngle = fireAngle;
		fireRange.range = range;
		mFriendsFireRange.add(fireRange);
	}

	public void scan(int tick) {
		try {
			mTick = tick;
			int mTankId = mDatabase.getMyTankId();
			int nowX = mDatabase.getNowX();
			int nowY = mDatabase.getNowY();
			List<TankGameInfo> friends = mDatabase.getFriendTanks();
			Iterator<TankGameInfo> iterator = mDatabase.getAllTanks().iterator();

			TankGameInfo leader = mDatabase.getLeader();

			int enemyId = -1;
			int minDistance = Integer.MAX_VALUE;
			mFriendsFireRange.clear();
			int nearEnemyId = -1;
			int nearMinDistance = Integer.MAX_VALUE;
			mEnemyCanFire.clear();
			while (iterator.hasNext()) {
				TankGameInfo enemyTank = (TankGameInfo) iterator.next();
				int tankid = enemyTank.id;
				if (mDatabase.isFriend(tankid)) {
					caculateFriendsFireRange(nowX, nowY, enemyTank);
					continue;
				} else if (tankid == mTankId) {
					continue;
				}
				int distance = Utils.distanceTo(leader.x, leader.y, enemyTank.x, enemyTank.y);
				int nearDis = Utils.distanceTo(nowX, nowY, enemyTank.x, enemyTank.y);
				if (distance < minDistance) {
					minDistance = distance;
					enemyId = enemyTank.getId();
				}

				if (nearDis < nearMinDistance) {
					nearMinDistance = nearDis;
					nearEnemyId = enemyTank.id;
				}

				boolean inFireBlock = mDatabase.fireInBlocks(nowX, nowY, enemyTank.x, enemyTank.y);
				if (!inFireBlock) {
					mEnemyCanFire.add(enemyTank);
				}
			}

			if (enemyId == -1) {
				log.debug("No Target found");
				return;
			}

			mTargetTankId = enemyId;
			mNearestTankId = nearEnemyId;
			
			TankGameInfo tank = mDatabase.getTankById(mTargetTankId);
			mTargetTank = new TankGameInfo(mTargetTankId, tank.x, tank.y, tank.r, tank.hp);
			boolean fireBlock = mDatabase.fireInBlocks(nowX, nowY, mTargetTank.x, mTargetTank.y);
			if(fireBlock) {
				TankGameInfo nearTank = mDatabase.getTankById(nearEnemyId);
				mTargetTank = new TankGameInfo(nearTank.id, nearTank.x, nearTank.y, nearTank.r, nearTank.hp);
			}

//			TankGameInfo tank = getNearestCanFireTank();
//			if (tank != null) {
//				mTargetTank = new TankGameInfo(tank.id, tank.x, tank.y, tank.r, tank.hp);
//			}
//			if (mTargetTank == null) {
//				TankGameInfo nearTank = mDatabase.getTankById(nearEnemyId);
//				if (nearTank != null) {
//					mTargetTank = new TankGameInfo(nearTank.id, tank.x, tank.y, tank.r, tank.hp);
//				}
//			}
//			if (tick > 600) {
//				TankGameInfo lowHpTank = getLowHpTank();
//				boolean fireBlock = mDatabase.fireInBlocks(nowX, nowY, lowHpTank.x, lowHpTank.y);
//				if (lowHpTank != null && !fireBlock) {
//					mTargetTank = new TankGameInfo(lowHpTank.id, lowHpTank.x, lowHpTank.y, lowHpTank.r, lowHpTank.hp);
//				} else {
//
//				}
//			}
			log.debug(String.format("[T%d][AttackTarget]->%s", mTick, mTargetTank.toString()));

		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}

	}

	public TankGameInfo getNearestCanFireTank() {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int minDis = Integer.MAX_VALUE;
		TankGameInfo leader = mDatabase.getLeader();
		TankGameInfo target = null;
		for (TankGameInfo tank : mEnemyCanFire) {
			int dis = Utils.distanceTo(leader.x, leader.y, tank.x, tank.y);
			if (dis < minDis) {
				minDis = dis;
				target = tank;
			}
		}
		return target;
	}

	public TankGameInfo getLowHpTank() {
		int minHp = Integer.MAX_VALUE;
		TankGameInfo ret = null;
		for (TankGameInfo tank : mEnemyCanFire) {
			if (tank.hp < minHp) {
				minHp = tank.hp;
				ret = tank;
			}
		}
		return ret;

	}

	public List<FireRange> getFriendsFireRange() {
		return mFriendsFireRange;
	}

	public TankGameInfo getTargetTank() {
		return mTargetTank;
	}

	public int getTargetTankId() {
		return mTargetTankId;
	}

	public TankGameInfo getNearestTank() {
		if (mNearestTankId < 0) {
			return null;
		}
		return mDatabase.getTankById(mNearestTankId);
	}

}
