package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.BulletInfo;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankMapProjectile;
import com.hisense.codewar.utils.Utils;

/**
 * 以自己为中心，扫描半径内的来袭子弹
 * 
 * @author hanzheng
 *
 */
public class CombatWarningRadar {
	// 雷达范围内弹道数据
	private List<BulletInfo> mBulletInfos;
	private CombatRealTimeDatabase mDatabase;
	// 雷达扫描半径
	private int SCAN_RADIUS = 400;
	private static final Logger log = LoggerFactory.getLogger(CombatRealTimeDatabase.class);

	public CombatWarningRadar(CombatRealTimeDatabase database) {
		mDatabase = database;
		mBulletInfos = new ArrayList<BulletInfo>();
		SCAN_RADIUS = AppConfig.RADAR_SCAN_RADIUS;
	}

	public void scan() {
		int mTankId = mDatabase.getMyTankId();

		List<TankMapProjectile> list = mDatabase.geTankMapProjectiles();
		//扫描
		for (TankMapProjectile projectile : list) {
			if (projectile.tankid == mDatabase.getMyTankId()) {
				continue;
			}
			if (inScanRange(projectile)) {
				scanBullets(projectile);
			}
		}
		
		//扫描威胁数据
		scanThreatBullet();
	}

	// 在雷达范围内
	protected boolean inScanRange(TankMapProjectile projectile) {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();

		int distance = Utils.distanceTo(nowX, nowY, projectile.x, projectile.y);
		return distance <= SCAN_RADIUS;
	}

	// 扫描弹道
	protected void scanBullets(TankMapProjectile projectile) {
		boolean existTank = false;
		boolean hasSameR = false;
		boolean hasSameXY = false;
		int tickIndex = -1;
		Iterator<BulletInfo> iterator = mBulletInfos.iterator();
		while (iterator.hasNext()) {
			BulletInfo bulletInfo = iterator.next();
			if (bulletInfo.getTankId() == projectile.tankid) {
				existTank = true;
				if (bulletInfo.getR() == projectile.r) {
					hasSameR = true;
					if (hasSameR) {
						if (bulletInfo.getStartX() == projectile.x && bulletInfo.getStartY() == projectile.y) {
							hasSameXY = true;
							if (!hasSameXY) {
								tickIndex = bulletInfo.isChild(projectile.x, projectile.y);
								if (tickIndex > 0) {
									// 归属已知弹道，更新数据
									bulletInfo.setCurrentX(projectile.x);
									bulletInfo.setCurrentY(projectile.y);
									bulletInfo.setTicks(tickIndex);
								}
							}
						}
					}
				}
				break;
			}

		}
//		for(BulletInfo bulletInfo : mBulletInfos) {
//			if(bulletInfo.getTankId() == projectile.tankid) {
//				existTank = true;
//				if(bulletInfo.getR() == projectile.r) {
//					hasSameR = true;
//					if(hasSameR) {
//						if(bulletInfo.getmStartX() == projectile.x && bulletInfo.getmStartY() == projectile.y) {
//							hasSameXY = true;	
//							if(!hasSameXY) {
//								tickIndex = bulletInfo.isChild(projectile.x, projectile.y);
//							}
//						}
//					}
//				}
//				break;
//			}
//		}

		// 不存在此坦克弹道，为新弹道
		if (!existTank) {
			insertBulletsInfo(projectile);
			return;
		}
		// 存在此坦克弹道，但R值不同，为新弹道
		if (!hasSameR) {
			insertBulletsInfo(projectile);
			return;
		}
		// R值也相同,但是startXY相同，为新弹道
		if (hasSameXY) {
			insertBulletsInfo(projectile);
			return;
		}

		// 此处R值相同，需要用公式算该子弹是否归属目标弹道
		// 公式返回-1，说明为新弹道
		if (tickIndex < 0) {
			insertBulletsInfo(projectile);
			return;
		}

	}

	protected void insertBulletsInfo(TankMapProjectile projectile) {
		BulletInfo bulletInfo = new BulletInfo(projectile.tankid, projectile.x, projectile.y, projectile.r);
		mBulletInfos.add(bulletInfo);
	}

	// 扫描威胁弹道
	protected void scanThreatBullet() {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		Iterator<BulletInfo> iterator = mBulletInfos.iterator();
		while (iterator.hasNext()) {
			BulletInfo bulletInfo = iterator.next();
			//弹道超时，移除
			if(!bulletInfo.isAlive()) {
				iterator.remove();
			}
			//发现来袭子弹
			if (BulletInfo.willHit(bulletInfo.getCurrentX(), bulletInfo.getCurrentY(), bulletInfo.getR(), nowX, nowY,
					AppConfig.TANK_WIDTH)) {
				
			}
		}
	}

}
