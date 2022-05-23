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
import com.hisense.codewar.model.TankGameActionType;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankGamePlayInterface;
import com.hisense.codewar.model.TankMapProjectile;
import com.hisense.codewar.utils.PoisionCircleUtils;

public class AutoBotsPlayer implements TankGamePlayInterface {

	private Random mRandom = new Random();
	private AtomicInteger mTick;
	private int mTankId = 0;
	private FireHelper mFireHelper;
	private CombatMovementHelper mMovementHelper;
	private MoveMentRadar mMoveMentRadar;
	private CombatAttackRadar mAttackRadar;
	private CombatWarningRadar mCombatWarningRadar;
	private CombatRealTimeDatabase mCombatRealTimeDatabase;
	private PoisionCircleUtils mPoisionCircleUtils;
	private static final Logger log = LoggerFactory.getLogger(AutoBotsPlayer.class);

	public AutoBotsPlayer() {
		// TODO Auto-generated constructor stub
		mTick = new AtomicInteger();
		mPoisionCircleUtils = new PoisionCircleUtils();
		mCombatRealTimeDatabase = new CombatRealTimeDatabase();
		mAttackRadar = new CombatAttackRadar(mCombatRealTimeDatabase);
		mFireHelper = new FireHelper(mCombatRealTimeDatabase, mAttackRadar);
		mMovementHelper = new CombatMovementHelper(mCombatRealTimeDatabase, mAttackRadar, mFireHelper);
		mCombatWarningRadar = new CombatWarningRadar(mCombatRealTimeDatabase, mMovementHelper);
		mMoveMentRadar = new MoveMentRadar(mCombatRealTimeDatabase, mAttackRadar, mMovementHelper, mPoisionCircleUtils);

	}

	@Override
	public void gametick(ITtank tank) {
		// TODO Auto-generated method stub
		try {
			boolean canFire = mFireHelper.canFire();
			boolean needDodge = mMovementHelper.needDodge(mTick.get());
			boolean canMove = mMovementHelper.canMove();
			log.debug(String.format("[T%d][Status] canFire[%b]needDodge[%b]", mTick.get(), canFire, needDodge));
			if (needDodge) {
				boolean dodgeDone = mMovementHelper.dodge(tank, mTick.get());
				if (!dodgeDone&&canMove) {
					mMovementHelper.move(tank, mTick.get());
				}
			} else if (canFire) {
				// randomMove(tank);
				boolean fireBlock = false;
				TankGameInfo enemyTank = mAttackRadar.getTargetTank();
				int nowX = mCombatRealTimeDatabase.getNowX();
				int nowY = mCombatRealTimeDatabase.getNowY();
				if (enemyTank != null) {
					fireBlock = mCombatRealTimeDatabase.fireInBlocks(nowX, nowY, enemyTank.x, enemyTank.y);
					if (fireBlock && canMove) {
						mMovementHelper.move(tank, mTick.get());
					} else {
						mFireHelper.fire(tank);
					}
				}

			} else if (canMove) {
				mMovementHelper.move(tank, mTick.get());
				// mMovementHelper.lock(tank, mTick.get());
			} else {
				mMovementHelper.lock(tank, mTick.get());
			}

		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}

	}

	protected void randomMove(ITtank tank) {
		int rr = 0;
		if (mTick.get() % 100 == 0) {

			rr = mRandom.nextInt(360);

		}

		mMovementHelper.addMoveByTick(rr, 1);

		if (mTick.get() % 5 == 0) {
			Random random = new Random();
			int r = random.nextInt(360);
			mMovementHelper.addMoveByTick(rr, 1);
			tank.tank_action(TankGameActionType.TANK_ACTION_FIRE, 0);
		} else {
			mMovementHelper.addMoveByTick(rr, 2);
		}
	}

	@Override
	public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles, int r,
			List<HitInfo> hits) {
		// TODO Auto-generated method stub
		try {
			long start = System.currentTimeMillis();
			mTick.getAndIncrement();
			mTankId = tank.id;
			mPoisionCircleUtils.updateR(r);
			mFireHelper.reload(mTick.get());
			mCombatRealTimeDatabase.setMyTankId(mTankId);
			mCombatRealTimeDatabase.updateAllTanks(tanks);
			mCombatRealTimeDatabase.updateProjectiles(projectiles);
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
		mPoisionCircleUtils.reset();
	}

	@Override
	public void gameend(ITtank tank) {
		// TODO Auto-generated method stub
		log.info(String.format("###########TANK[%d][%d]BattleEND############", mTankId, 1));
	}

}
