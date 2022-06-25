package com.hisense.codewar.data;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.player.AutoBotsPlayer;
import com.hisense.codewar.utils.Utils;

public class CombatController {
	CombatMovementHelper mHelper;
	FireHelper mFireHelper;
	CombatAttackRadar mRadar;
	CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(CombatController.class);

	public CombatController(CombatMovementHelper helper, FireHelper fireHelper, CombatAttackRadar radar,
			CombatRealTimeDatabase database) {
		mHelper = helper;
		mFireHelper = fireHelper;
		mRadar = radar;
		mDatabase = database;
	}

	public void gameTick2(ITtank tank, int tick) {
		try {

			boolean canTrack = mHelper.needTrack(tick);
			// boolean canTrack = false;
			// boolean canMove = false;
			boolean canFire = mFireHelper.canFire();
			boolean needDodge = mHelper.needDodage(tank, tick);
			boolean canMove = mHelper.canMove();

			boolean fireBlock = false;
			TankGameInfo enemyTank = mRadar.getTargetTank();
			if (enemyTank == null) {
				mHelper.move(tank, tick);
				return;
			}
			int nowX = mDatabase.getNowX();
			int nowY = mDatabase.getNowY();
			List<Bullet> bullets = mDatabase.getBullets();
			int distance = Utils.distanceTo(nowX, nowY, enemyTank.x, enemyTank.y);
			if (needDodge) {
				mHelper.dodgeV4(tank, 3, tick);
				int ret = tank.getRestActions();
				if (ret <= 0) {
					return;
				} else {
					boolean a = mHelper.moveAndFire(enemyTank.x, enemyTank.y, tank, ret, tick);

				}
			} else if (canFire) {
				fireBlock = mDatabase.fireInBlocks(nowX, nowY, enemyTank.x, enemyTank.y);
				if (!fireBlock) {
					mHelper.moveAndFire(enemyTank.x, enemyTank.y, tank, 3, tick);
					int ret = tank.getRestActions();
					if (ret == 0) {
						return;
					} else {
						if (needDodge) {
							mHelper.dodgeV4(tank, ret, tick);
							return;
						} else if (canTrack) {
							mHelper.track(tank, ret, tick);
							return;
						}
					}
				}

				// 遮挡，无法射击
				else {
					if (canTrack) {
						mHelper.track(tank, tick);
						return;
					}
					if (needDodge) {
						mHelper.dodgeV4(tank, 3, tick);
						return;
					} else if (canMove) {
						mHelper.move(tank, tick);
						return;
					}
				}

			} else {
				mHelper.move(tank, tick);
				return;
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}

	}

	public void gameTick(ITtank tank, int tick) {
		try {

			boolean canTrack = mHelper.needTrack(tick);
			// boolean canTrack = false;
			// boolean canMove = false;
			boolean canFire = mFireHelper.canFire();
			boolean needDodge = mHelper.needDodage(tank, tick);
			boolean canMove = mHelper.canMove();

			boolean fireBlock = false;
			TankGameInfo enemyTank = mRadar.getTargetTank();
			if (enemyTank == null) {
				mHelper.move(tank, tick);
				return;
			}
			int nowX = mDatabase.getNowX();
			int nowY = mDatabase.getNowY();
			List<Bullet> bullets = mDatabase.getBullets();
			int distance = Utils.distanceTo(nowX, nowY, enemyTank.x, enemyTank.y);
			TankGameInfo nearTank = mRadar.getEnemyTankNear(AppConfig.FIRE_DISTANCE);
			if (nearTank != null) {
				if (canFire) {
					mFireHelper.fire2(tank, nearTank);
					int ret = tank.getRestActions();
					if (ret <= 0) {
						return;
					} else {
						if (needDodge) {
							mHelper.dodgeV4(tank, ret, tick);
							return;
						} else {
							mHelper.move(tank, ret, tick);
							return;
						}
					}
				} else if (needDodge) {
					mHelper.dodgeV4(tank, 3, tick);
					int ret = tank.getRestActions();
					if (ret <= 0) {
						return;
					} else {
						mHelper.move(tank, ret, tick);
						return;
					}
				} else {
					mHelper.move(tank, tick);
				}
			} else if (needDodge) {
				mHelper.dodgeV4(tank, 3, tick);
				int ret = tank.getRestActions();
				if (ret <= 0) {
					return;
				} else {
					if (canMove) {
						mHelper.move(tank, ret, tick);
						return;
					}
				}
			} else if (canFire) {
				fireBlock = mDatabase.fireInBlocks(nowX, nowY, enemyTank.x, enemyTank.y);
				if (!fireBlock) {
					if (distance <= AppConfig.FIRE_DISTANCE) {
						mFireHelper.fire2(tank, enemyTank);
						int ret = tank.getRestActions();
						if (ret == 0) {
							return;
						} else {
							mHelper.move(tank, ret, tick);
						}
					}else {
						if (canTrack) {
							mHelper.track(tank, tick);
							return;
						}
						if (needDodge) {
							mHelper.dodgeV4(tank, 3, tick);
							return;
						} else if (canMove) {
							mHelper.move(tank, tick);
							return;
						}
					}
				}
				// 遮挡，无法射击
				else {
					if (canTrack) {
						mHelper.track(tank, tick);
						return;
					}
					if (needDodge) {
						mHelper.dodgeV4(tank, 3, tick);
						return;
					} else if (canMove) {
						mHelper.move(tank, tick);
						return;
					}
				}
			} else if (!canFire) {
				mHelper.move(tank, tick);
			}

			else if (canTrack) {
				mHelper.track(tank, tick);
				return;
			} else if (canMove) {
				mHelper.move(tank, tick);
				return;
			} else {
				mHelper.move(tank, tick);
				return;
			}

		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());

		}

	}

}
