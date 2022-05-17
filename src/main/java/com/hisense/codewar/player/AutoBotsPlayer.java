package com.hisense.codewar.player;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.data.CombatMovementHelper;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.data.CombatWarningRadar;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGameActionType;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankGamePlayInterface;
import com.hisense.codewar.model.TankMapProjectile;

public class AutoBotsPlayer implements TankGamePlayInterface {
	private Random mRandom = new Random();
	private AtomicInteger mTick;
	private int mTankId = 0;
	private CombatMovementHelper mMovementHelper;
	private CombatWarningRadar mCombatWarningRadar;
	private CombatRealTimeDatabase mCombatRealTimeDatabase;
	private static final Logger log = LoggerFactory.getLogger(AutoBotsPlayer.class);

	public AutoBotsPlayer() {
		// TODO Auto-generated constructor stub
		mTick = new AtomicInteger();
		mMovementHelper = new CombatMovementHelper();
		mCombatRealTimeDatabase = new CombatRealTimeDatabase();
		mCombatWarningRadar = new CombatWarningRadar(mCombatRealTimeDatabase, mMovementHelper);
	}

	@Override
	public void onstart(int i) {
		// TODO Auto-generated method stub
		mCombatRealTimeDatabase.reset();
		mCombatWarningRadar.reset();
		mMovementHelper.reset();
	}

	@Override
	public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles) {
		// TODO Auto-generated method stub
		try {
			mTankId = tank.id;
			mCombatRealTimeDatabase.setMyTankId(mTankId);
			mCombatRealTimeDatabase.updateAllTanks(tanks);
			mCombatRealTimeDatabase.updateProjectiles(projectiles);
			mCombatWarningRadar.scan(mTick.getAndIncrement());

		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}

	}

	@Override
	public void gametick(ITtank tank) {
		// TODO Auto-generated method stub
		try {
			boolean needDodge = mMovementHelper.needDodge(mTick.get());
			boolean canMove = mMovementHelper.canMove();
			if (needDodge) {
				boolean dodgeDone = mMovementHelper.dodge(tank, mTick.get());
				if (!dodgeDone) {
					// 没有闪躲，可以移动或攻击
					randomMove(tank);
				}
			} else if (canMove) {
				randomMove(tank);
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

}
