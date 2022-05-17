package com.hisense.codewar.player;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.data.CombatMovementHelper;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.data.CombatWarningRadar;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankGamePlayInterface;
import com.hisense.codewar.model.TankMapProjectile;
import com.hisense.codewar.utils.Utils;

public class AutoBotsPlayer implements TankGamePlayInterface {
	private int mTankId = 0;
	private CombatMovementHelper mMovementHelper;
	private CombatWarningRadar mCombatWarningRadar;
	private CombatRealTimeDatabase mCombatRealTimeDatabase;
	private static final Logger log = LoggerFactory.getLogger(AutoBotsPlayer.class);

	public AutoBotsPlayer() {
		// TODO Auto-generated constructor stub
		mMovementHelper = new CombatMovementHelper();
		mCombatRealTimeDatabase = new CombatRealTimeDatabase();
		mCombatWarningRadar = new CombatWarningRadar(mCombatRealTimeDatabase, mMovementHelper);
	}

	@Override
	public void onstart(int i) {
		// TODO Auto-generated method stub
		mCombatRealTimeDatabase.reset();
		mCombatWarningRadar.reset();
	}

	@Override
	public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles) {
		// TODO Auto-generated method stub
		try {
			mTankId = tank.id;
			mCombatRealTimeDatabase.setMyTankId(mTankId);
			mCombatRealTimeDatabase.updateAllTanks(tanks);
			mCombatRealTimeDatabase.updateProjectiles(projectiles);
			mCombatWarningRadar.scan();

		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}

	}

	@Override
	public void gametick(ITtank tank) {
		// TODO Auto-generated method stub
		try {
			boolean canMove = mMovementHelper.canMove();
			if (canMove) {
				mMovementHelper.move(tank);
			}

		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}

	}

}
