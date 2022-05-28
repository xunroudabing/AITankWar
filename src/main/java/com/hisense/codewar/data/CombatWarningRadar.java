package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.algorithm.DodageLvl1Algorithm;
import com.hisense.codewar.algorithm.DodageLvl2Algorithm;
import com.hisense.codewar.algorithm.IDodageAlgorithm;
import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.Bullet.DodgeSuggestion;
import com.hisense.codewar.model.BulletInfo;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankMapProjectile;
import com.hisense.codewar.model.ThreatTarget;
import com.hisense.codewar.utils.Utils;

/**
 * 以自己为中心，扫描半径内的来袭子弹
 * 
 * @author hanzheng
 *
 */
public class CombatWarningRadar {

	public static void main(String[] args) {

	}

	private int mThreatBullets = 0;
	private int mTick = 0;
	// 威胁列表，敌人tankid,弹道数据等
	private List<ThreatTarget> mThreatTargets;
	// 雷达范围内弹道数据
	private List<BulletInfo> mBulletInfos;
	private CombatRealTimeDatabase mDatabase;
	private CombatMovementHelper mMoveHelper;
	// 子弹躲避算法
	private IDodageAlgorithm mDodageAlgorithm;
	// 雷达扫描半径
	private int SCAN_RADIUS = 400;
	private static final Logger log = LoggerFactory.getLogger(CombatWarningRadar.class);

	public CombatWarningRadar(CombatRealTimeDatabase database, CombatMovementHelper helper) {
		mDatabase = database;
		mMoveHelper = helper;
		mBulletInfos = new ArrayList<BulletInfo>();
		mThreatTargets = new ArrayList<ThreatTarget>();
		SCAN_RADIUS = AppConfig.RADAR_SCAN_RADIUS;
		mDodageAlgorithm = new DodageLvl2Algorithm(mDatabase, mMoveHelper);
	}

	public void reset() {
		mTick = 0;
		mThreatBullets = 0;
		mBulletInfos.clear();
		mThreatTargets.clear();
	}

	public void scan(int tick) {
		mTick = tick;
		int mTankId = mDatabase.getMyTankId();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		List<TankMapProjectile> list = mDatabase.geTankMapProjectiles();
		// 扫描
		for (TankMapProjectile projectile : list) {
			log.debug(projectile.toString());
			if (projectile.tankid == mDatabase.getMyTankId()) {
				continue;
			}
			// bullet出界了也要考虑
			if (inScanRange(projectile)) {
				scanBullets(projectile);
			}
		}

		// 扫描威胁数据
		int threatBulletSize = mDodageAlgorithm.scan(mDatabase.getBullets(), nowX, nowY, tick);
		// scanThreatBullet();

		// 扫描威胁目标
		// scanThreadTarget();
	}
	public int getThreatBullets() {
		return mThreatBullets;
	}
	/**
	 * 获取威胁列表
	 * 
	 * @return
	 */

	public List<ThreatTarget> getThreatTarget() {
		return mThreatTargets;
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
		Iterator<Bullet> iterator = mDatabase.getBullets().iterator();
		boolean isExist = false;
		while (iterator.hasNext()) {
			Bullet bullet = (Bullet) iterator.next();
			if (projectile.tankid == bullet.tankid && projectile.r == bullet.r) {
				// 属于同一弹道,不处理
				boolean isChild = bullet.isChild(projectile.x, projectile.y);
				if (isChild) {
					bullet.currentX = projectile.x;
					bullet.currentY = projectile.y;
					bullet.updateTick = mTick;// 刷新更新时间
					isExist = true;
					break;
				}
			}

		}
		if (!isExist) {
			Bullet bullet = new Bullet();
			bullet.startX = projectile.x;
			bullet.startY = projectile.y;
			bullet.currentX = projectile.x;
			bullet.currentY = projectile.y;
			bullet.r = projectile.r;
			bullet.tankid = projectile.tankid;
			bullet.createTick = mTick;
			bullet.updateTick = mTick;
			mDatabase.getBullets().add(bullet);
			log.debug(String.format("[T%d][RadarWarning] %s", mTick, bullet.toString()));
		}

	}

	protected void insertBulletsInfo(TankMapProjectile projectile) {
		BulletInfo bulletInfo = new BulletInfo(projectile.tankid, projectile.x, projectile.y, projectile.r);
		mBulletInfos.add(bulletInfo);
	}

}
