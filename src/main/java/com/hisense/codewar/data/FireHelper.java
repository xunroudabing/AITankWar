package com.hisense.codewar.data;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.FireRange;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameActionType;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;
import com.hisense.codewar.wave.WaveSurfing;

public class FireHelper {
	private Random mRandom = new Random();
	private CombatStatistics mStatistics;
	private int mTick = 60;
	CombatEnemyDatabase mEnemyDatabase;
	CombatRealTimeDatabase mDatabase;
	CombatAttackRadar mAttackRadar;
	private static final Logger log = LoggerFactory.getLogger(FireHelper.class);

	public FireHelper(CombatRealTimeDatabase database, CombatAttackRadar attackRadar,
			CombatEnemyDatabase enemyDatabase) {
		mDatabase = database;
		mAttackRadar = attackRadar;
		mEnemyDatabase = enemyDatabase;
		mTick = AppConfig.FIRE_SPAN;
	}

	public void setStatistics(CombatStatistics s) {
		mStatistics = s;
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
		boolean guessFireEnable = false;
		boolean guessFire = false;
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

		// 是否有队友正在向目标射击,有则进行预测射击
		boolean friendShooting = CombatFriendBulletDatabase.getInstance().exist(target.id, mTick, mtankid);
		if (friendShooting) {
			guessFireEnable = true;
		}
		//预测射击
		int dest = bestDest;
		int aim = 0;
		if (guessFireEnable) {
			// 波统计瞄准角度
			aim = (int) waveSurfing.getBestMatchFireAngle(target.id);
			dest = dest + aim;
		}

		// 避免误伤
		if (willHitFriends(dest)) {
			return false;
		}
		// 射界内，不需要转向，直接开火
		if (Math.abs(dest - heading) <= range) {
			tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		} else {
			boolean b = mRandom.nextBoolean();
			int seed = b ? 1 : -1;
			int r = mRandom.nextInt(range);
			dest += seed * r / 2;
			// 避免误伤
			if (willHitFriends(dest)) {
				return false;
			}
			tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, dest);
		}

		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		log.debug(
				String.format("[WaveFire]guessFire[%b]me[%d]pos[%d,%d]dest[%d]bestDest[%d]aim[%d]-->tankid[%d]pos[%d,%d]heading[%d]",
						guessFireEnable,mtankid, nowX, nowY, dest, bestDest, aim, target.id, target.x, target.y, target.r));
		mTick = 0;
		// 创建波
		waveSurfing.createWave(target, mTick);
		if (mStatistics != null) {
			mStatistics.fireCounter();
		}
		return true;
	}

	public boolean fire(ITtank tank) {
		boolean guessFireEnable = true;
		boolean guessFire = false;
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
		// 是否有队友正在向目标射击,有则进行预测射击
//		boolean friendShooting = CombatFriendBulletDatabase.getInstance().exist(target.id, mTick, mtankid);
//		if (friendShooting) {
//			guessFireEnable = true;
//		}
		// 预测射击
		int guessDest = 0;
		int orignDest = 0;
		if (guessFireEnable) {
			Position guessPosition = mEnemyDatabase.guessPositionByPattern(target.id);
			if (guessPosition != null) {
				guessDest = Utils.angleTo(nowX, nowY, guessPosition.x, guessPosition.y);
				orignDest = dest;
				dest = guessDest;
				guessFire = true;
				log.debug(String.format("[Guess-Fire]me[%d]-->enemyid[%d]currentPos[%d,%d]guessPos[%d,%d]", mtankid,
						target.id, target.x, target.y, guessPosition.x, guessPosition.y));
			}
		}
		// 避免误伤
		if (willHitFriends(dest)) {
			return false;
		}
		// 射界内，不需要转向，直接开火
		if (heading > Math.abs(dest - range) && heading < Math.abs(dest + range)) {
			tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		} else {
			// 非预测射击加随机角度
			if (!guessFire) {
				boolean b = mRandom.nextBoolean();
				int seed = b ? 1 : -1;
				int r = mRandom.nextInt(range);
				dest += seed * r;
			}
			// 避免误伤
			if (willHitFriends(dest)) {
				return false;
			}
			tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, dest);
		}

		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, dest);
		log.debug(String.format(
				"[Fire]me[%d]guessFire[%b]pos[%d,%d]dest[%d]oridest[%d]-->tankid[%d]pos[%d,%d]heading[%d]", mtankid,
				guessFire, nowX, nowY, dest, orignDest, target.id, target.x, target.y, target.r));
		mTick = 0;
		if (mStatistics != null) {
			mStatistics.fireCounter();
		}
		CombatFriendBulletDatabase.getInstance().createBullet(mtankid, target.id, target.x, target.y, dest);
		return true;
	}

	public boolean willHitFriends(int dest) {
		boolean ret = false;
		List<FireRange> friends = mAttackRadar.getFriendsFireRange();
		for (FireRange fireRange : friends) {
			// 在友军射界内
			if (dest >= Math.abs(fireRange.fireAngle - fireRange.range / 2 - 10)
					&& dest <= Math.abs(fireRange.fireAngle + fireRange.range / 2 + 10)) {
				ret = true;
				break;
			}

		}
		return ret;
	}

}
