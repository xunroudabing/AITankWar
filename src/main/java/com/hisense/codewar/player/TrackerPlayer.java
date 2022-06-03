package com.hisense.codewar.player;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.data.CombatAttackRadar;
import com.hisense.codewar.data.CombatMovementHelper;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.data.CombatWarningRadar;
import com.hisense.codewar.data.FireHelper;
import com.hisense.codewar.data.MoveMentRadar;
import com.hisense.codewar.model.HitInfo;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankGamePlayInterface;
import com.hisense.codewar.model.TankMapProjectile;

public class TrackerPlayer implements TankGamePlayInterface {

	private Random mRandom = new Random();
	private AtomicInteger mTick;
	private int mTankId = 0;
	private FireHelper mFireHelper;
	private CombatMovementHelper mMovementHelper;
	private MoveMentRadar mMoveMentRadar;
	private CombatAttackRadar mAttackRadar;
	private CombatWarningRadar mCombatWarningRadar;
	private CombatRealTimeDatabase mCombatRealTimeDatabase;
	private static final Logger log = LoggerFactory.getLogger(TrackerPlayer.class);

	public TrackerPlayer() {
		// TODO Auto-generated constructor stub
		mTick = new AtomicInteger();
		mCombatRealTimeDatabase = new CombatRealTimeDatabase();
		mAttackRadar = new CombatAttackRadar(mCombatRealTimeDatabase);
		mFireHelper = new FireHelper(mCombatRealTimeDatabase, mAttackRadar,null);
		mMovementHelper = new CombatMovementHelper(mCombatRealTimeDatabase, mAttackRadar, mFireHelper);
		mCombatWarningRadar = new CombatWarningRadar(mCombatRealTimeDatabase, mMovementHelper);
		mMoveMentRadar = new MoveMentRadar(mCombatRealTimeDatabase, mAttackRadar, mMovementHelper);

	}

	@Override
	public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles, int r,
			List<HitInfo> hits) {
		// TODO Auto-generated method stub
		try {
			long start = System.currentTimeMillis();
			mTick.getAndIncrement();
			mTankId = tank.id;
			mFireHelper.reload(mTick.get());
			mCombatRealTimeDatabase.setMyTankId(mTankId);
			mCombatRealTimeDatabase.updateAllTanks(tanks);
			mCombatRealTimeDatabase.updateProjectiles(projectiles);
			mCombatRealTimeDatabase.updatePoisionR(r);
			mCombatWarningRadar.scan(mTick.get());
			mAttackRadar.scan(mTick.get());
			mMoveMentRadar.scan(mTick.get());
			long end = System.currentTimeMillis();
			long cost = end - start;
			log.debug(String.format("[ScanCost]PoisionR[%d][%d]cost %d ms", r, mTick.get(), cost));

		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}

	}

	@Override
	public void gametick(ITtank tank) {
		// TODO Auto-generated method stub
		try {
			boolean canFire = mFireHelper.canFire();
			boolean needDodge = mMovementHelper.needDodge(mTick.get());
			boolean canMove = mMovementHelper.canMove();
			log.debug("needDodge " + needDodge + " canMove " + canMove + " canfire " + canFire);
			log.debug(String.format("[T%d][Status] canFire[%b]needDodge[%b]", mTick.get(), canFire, needDodge));
			if (needDodge) {
				boolean dodgeDone = mMovementHelper.dodge(tank, mTick.get());
			}  else {
				mMovementHelper.move(tank, mTick.get());
			} 

		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}

	}

	@Override
	public void gamestart(ITtank tank) {
		// TODO Auto-generated method stub
		log.info(String.format("###########TANK[%d][%d]BattleSTART############", mTankId, 1));
		mCombatRealTimeDatabase.reset();
		mCombatWarningRadar.reset();
		mAttackRadar.reset();
		mMovementHelper.reset();
		mFireHelper.reset();
		mMoveMentRadar.reset();
		mTick.set(0);
		mCombatRealTimeDatabase.setBlocks(tank.getBlocks());
	}

	@Override
	public void gameend(ITtank tank) {
		// TODO Auto-generated method stub

	}

}
