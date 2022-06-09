package com.hisense.codewar.player;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.antigrave.AntiGraveMover;
import com.hisense.codewar.data.CombatAttackRadar;
import com.hisense.codewar.data.CombatController;
import com.hisense.codewar.data.CombatEnemyDatabase;
import com.hisense.codewar.data.CombatMovementHelper;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.data.CombatStatistics;
import com.hisense.codewar.data.CombatWarningRadar;
import com.hisense.codewar.data.FireHelper;
import com.hisense.codewar.data.MoveMentRadar;
import com.hisense.codewar.model.HitInfo;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankGamePlayInterface;
import com.hisense.codewar.model.TankMapProjectile;
import com.hisense.codewar.utils.Utils;
import com.hisense.codewar.wave.WaveSurfing;
import com.jfinal.kit.Ret;

public class AutoBotsPlayer implements TankGamePlayInterface {

	private Random mRandom = new Random();
	private AtomicInteger mTick;
	private int mTankId = 0;
	private CombatStatistics mCombatStatistics;
	private WaveSurfing mWaveSurfing;
	private CombatEnemyDatabase mEnemyDatabase;
	private AntiGraveMover mAntiGraveMover;
	private FireHelper mFireHelper;
	private CombatMovementHelper mMovementHelper;
	private MoveMentRadar mMoveMentRadar;
	private CombatAttackRadar mAttackRadar;
	private CombatWarningRadar mCombatWarningRadar;
	private CombatRealTimeDatabase mCombatRealTimeDatabase;
	private CombatController mController;
	private static final Logger log = LoggerFactory.getLogger(AutoBotsPlayer.class);

	public AutoBotsPlayer() {
		// TODO Auto-generated constructor stub
		mTick = new AtomicInteger();
		mCombatRealTimeDatabase = new CombatRealTimeDatabase();
		mEnemyDatabase = new CombatEnemyDatabase(mCombatRealTimeDatabase);
		mAttackRadar = new CombatAttackRadar(mCombatRealTimeDatabase);
		mFireHelper = new FireHelper(mCombatRealTimeDatabase, mAttackRadar, mEnemyDatabase);
		mMovementHelper = new CombatMovementHelper(mCombatRealTimeDatabase, mAttackRadar, mFireHelper);
		mCombatWarningRadar = new CombatWarningRadar(mCombatRealTimeDatabase, mMovementHelper);
		mMoveMentRadar = new MoveMentRadar(mCombatRealTimeDatabase, mAttackRadar, mMovementHelper);
		mAntiGraveMover = new AntiGraveMover(mCombatRealTimeDatabase, mAttackRadar, mMovementHelper, mFireHelper);
		mWaveSurfing = new WaveSurfing(mCombatRealTimeDatabase, mEnemyDatabase);
		mCombatStatistics = new CombatStatistics();
		mFireHelper.setStatistics(mCombatStatistics);
		mController = new CombatController(mMovementHelper, mFireHelper, mAttackRadar, mCombatRealTimeDatabase);
	}

	@Override
	public void gametick(ITtank tank) {
		// TODO Auto-generated method stub
		// log.info("gametick");
		long start = System.currentTimeMillis();
		mController.gameTick(tank, mTick.get());
//		try {
//
//			boolean canTrack = mMovementHelper.needTrack(mTick.get());
//			boolean canPolling = mMovementHelper.canPolling();
//			boolean canFire = mFireHelper.canFire();
//			boolean needDodge = mMovementHelper.needDodge(mTick.get());
//			boolean canMove = mMovementHelper.canMove();
//			log.debug(String.format("[T%d][Status] canFire[%b]needDodge[%b]", mTick.get(), canFire, needDodge));
//
//			if (needDodge) {
//				boolean dodgeDone = mMovementHelper.dodge2(tank, mTick.get());
//				if (dodgeDone) {
//					return;
//				}
//			} else if (canFire) {
//				// randomMove(tank);
//				boolean fireBlock = false;
//				TankGameInfo enemyTank = mAttackRadar.getTargetTank();
//				int nowX = mCombatRealTimeDatabase.getNowX();
//				int nowY = mCombatRealTimeDatabase.getNowY();
//				if (enemyTank != null) {
//					fireBlock = mCombatRealTimeDatabase.fireInBlocks(nowX, nowY, enemyTank.x, enemyTank.y);
//					int dis = Utils.distanceTo(nowX, nowY, enemyTank.x, enemyTank.y);
//					if (dis <= 80) {
//						boolean ret = mFireHelper.fire3(tank);
//						if (ret) {
//							return;
//						}
//						// 无法射击
//						if (!ret) {
//							if (needDodge) {
//								boolean dodgeDone = mMovementHelper.dodge2(tank, mTick.get());
//								if (dodgeDone) {
//									return;
//								}
//								if (!dodgeDone && canMove) {
//									mMovementHelper.move(tank, mTick.get());
//								}
//							}
//						}
//					} else if (fireBlock) {
//						if (needDodge) {
//							boolean dodgeDone = mMovementHelper.dodge2(tank, mTick.get());
//							if (dodgeDone) {
//								return;
//							} else if (!dodgeDone && canMove) {
//								boolean ret = mMovementHelper.move(tank, mTick.get());
//								if (ret) {
//									return;
//								}
//							}
//						}
//						if (canTrack) {
//							mMovementHelper.track(tank, mTick.get());
//						} else if (canMove) {
//							boolean ret = mMovementHelper.move(tank, mTick.get());
//							if (ret) {
//								return;
//							}
//						}
//					} else {
//						// boolean ret = mFireHelper.wavefire(tank,mWaveSurfing);
//						boolean ret = mFireHelper.fire3(tank);
//						// 无法射击
//						if (!ret) {
//							if (needDodge) {
//								boolean dodgeDone = mMovementHelper.dodge2(tank, mTick.get());
//								if (dodgeDone) {
//									return;
//								}
//								if (!dodgeDone && canMove) {
//									mMovementHelper.move(tank, mTick.get());
//								}
//							}
//						}
//					}
//				} else {
//					if (canMove) {
//						boolean ret = mMovementHelper.move(tank, mTick.get());
//						if (ret) {
//							return;
//						}
//					}
//				}
//
//			} else if (canTrack) {
//				mMovementHelper.track(tank, mTick.get());
//			} else if (canMove) {
//				boolean ret = mMovementHelper.move(tank, mTick.get());
//				// mMovementHelper.lock(tank, mTick.get());
//			} else if (canPolling) {
//				// mMovementHelper.polling(tank, mTick.get());
//			} else {
//				mMovementHelper.move(tank, mTick.get());
//			}
//
//		} catch (Exception e) {
//			// TODO: handle exception
//			log.error(e.fillInStackTrace().getLocalizedMessage());
//		}
		long end = System.currentTimeMillis();
		long cost = end - start;
		log.info(String.format("[GameTickCost][%d]tank[%d]cost %d ms", mTick.get(),mTankId, cost));
	}

	@Override
	public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles, int r,
			List<HitInfo> hits) {
		// TODO Auto-generated method stub
		// log.info("updatemap");
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
			mEnemyDatabase.scan(mTick.get());
			mAttackRadar.scan(mTick.get());
			mMoveMentRadar.scan(mTick.get());
			//mAntiGraveMover.move(mTick.get());
			// mWaveSurfing.scan(mTick.get());
			hitEnemyCount(hits);
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
		mEnemyDatabase.reset();
		mCombatWarningRadar.reset();
		mAttackRadar.reset();
		mMovementHelper.reset();
		mFireHelper.reset();
		mMoveMentRadar.reset();
		mAntiGraveMover.reset();
		mTick.set(0);
		mCombatRealTimeDatabase.setBlocks(tank.getBlocks());
	}

	@Override
	public void gameend(ITtank tank) {
		// TODO Auto-generated method stub
		log.info(String.format("###########TANK[%d][%d]BattleEND############", mTankId, 1));
		mCombatStatistics.show();
		mCombatRealTimeDatabase.reset();
		mCombatWarningRadar.reset();
		mAttackRadar.reset();
		mMovementHelper.reset();
		mFireHelper.reset();
		mMoveMentRadar.reset();
		mEnemyDatabase.reset();
		mAntiGraveMover.reset();
		mTick.set(0);
	}

	protected void hitEnemyCount(List<HitInfo> hits) {
		if (hits == null) {
			return;
		}
		int tankid = mCombatRealTimeDatabase.getMyTankId();
		for (HitInfo hitInfo : hits) {
			if (hitInfo.sTankid == tankid) {
				int enemyId = hitInfo.dTankid;
				log.info(String.format("[Battle-INFO][T%d]Me[%d]hit--->Enemy[%d]", mTick.get(), tankid, enemyId));
				mCombatStatistics.hitCounter();
			} else if (hitInfo.dTankid == tankid) {
				int enemyId = hitInfo.sTankid;
				mCombatStatistics.hitmeCounter();
				log.info(String.format("[Battle-INFO][T%d]Enemy[%d]hit--->Me[%d]", mTick.get(), enemyId, tankid));
			}
		}
	}

}
