package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.BulletInfo;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankMapProjectile;
import com.hisense.codewar.model.ThreatTarget;
import com.hisense.codewar.utils.Utils;
import com.jfinal.log.Log;

/**
 * 以自己为中心，扫描半径内的来袭子弹
 * 
 * @author hanzheng
 *
 */
public class CombatWarningRadar {
	private int mTick = 0;
	// 威胁列表，敌人tankid,弹道数据等
	private List<ThreatTarget> mThreatTargets;
	// 雷达范围内弹道数据
	private List<BulletInfo> mBulletInfos;
	private CombatRealTimeDatabase mDatabase;
	private CombatMovementHelper mMoveHelper;
	// 雷达扫描半径
	private int SCAN_RADIUS = 400;
	private static final Logger log = LoggerFactory.getLogger(CombatWarningRadar.class);

	public CombatWarningRadar(CombatRealTimeDatabase database, CombatMovementHelper helper) {
		mDatabase = database;
		mMoveHelper = helper;
		mBulletInfos = new ArrayList<BulletInfo>();
		mThreatTargets = new ArrayList<ThreatTarget>();
		SCAN_RADIUS = AppConfig.RADAR_SCAN_RADIUS;
	}

	public void reset() {
		mBulletInfos.clear();
		mThreatTargets.clear();
	}

	public void scan(int tick) {
		mTick = tick;
		int mTankId = mDatabase.getMyTankId();

		List<TankMapProjectile> list = mDatabase.geTankMapProjectiles();
		// 扫描
		for (TankMapProjectile projectile : list) {
			log.debug(projectile.toString());
			if (projectile.tankid == mDatabase.getMyTankId()) {
				continue;
			}
			if (inScanRange(projectile)) {
				scanBullets(projectile);
			}
		}

		// 扫描威胁数据
		scanThreatBullet();

		// 扫描威胁目标
		// scanThreadTarget();
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
			mDatabase.getBullets().add(bullet);
			log.debug(String.format("[T%d][RadarWarning] %s", mTick, bullet.toString()));
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
		int mHeading = mDatabase.getHeading();
		int targetUnhandle = 0;//未处理的且会击中我的目标
		int lastDodgeCost = 0;//上一个目标闪避耗时
		Iterator<Bullet> iterator2 = mDatabase.getBullets().iterator();
		while (iterator2.hasNext()) {
			Bullet bullet = (Bullet) iterator2.next();
			if (!bullet.isActive(mTick)) {
				iterator2.remove();
			}
			if (bullet.handled) {
				int distance = Utils.distanceTo(nowX, nowY, bullet.currentX, bullet.currentY);
				log.debug(String.format("%s distance[%d] is handled,continue", bullet.toString(), distance));
				continue;
			}
			boolean canHit = false;
			// 計算圓心到彈道的垂足 ，p1,p2为弹道 p3圆心
			Position p1 = new Position(bullet.currentX, bullet.currentY);
			Position p2 = Utils.getNextBulletByTick(bullet.currentX, bullet.currentY, bullet.r, 2);
			Position p3 = new Position(nowX, nowY);
			// 垂足
			Position p4 = Utils.getFoot(p1, p2, p3);

			// 坦克半径
			int c = AppConfig.TANK_WIDTH;
			// 垂足到圆心距离
			int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);
			// 会被击中
			if (a < c) {
				canHit = true;
			}

			if (!canHit) {
				continue;
			}
			//雷达当前威胁目标+1
			targetUnhandle++;
			int b = (int) Math.sqrt(c * c - a * a);

			// 总距离
			int totalDistance = Utils.distanceTo(p4.x, p4.y, p1.x, p1.y);
			// 子弹到我的距离
			int distance = totalDistance - b;
			// 剩余来袭时间
			int hitTickleft = distance / AppConfig.BULLET_SPEED;
			// 被击中，来不及躲
			if (hitTickleft <= 0) {
				log.debug(String.format("[T%d][HitMe]hitTickLeft[%d]%s", mTick, hitTickleft, bullet.toString()));
				continue;
			}
			// 计算最佳躲避方向，按最佳方向闪避,闪避耗时约为10tick
			// int dodgeAngle = Utils.getTargetRadius(p4.x, p4.y, nowX, nowY);
			int dodgeAngle = Utils.angleTo(nowX, nowY, p4.x, p4.y);
			// 所需移动距离
			int dodgeDistance = AppConfig.TANK_WIDTH - a;
			// 闪避所需时间 dodgeDistance / AppConfig.TANK_SPEED;
			int dodgeNeedTick = Utils.getTicks(dodgeDistance, AppConfig.TANK_SPEED);
			//如果有大于1个目标需要提前闪避，防止被集火
			if(targetUnhandle > 1) {
				dodgeNeedTick += lastDodgeCost;
			}else {
				// 为了保险提前2个tick进行闪避
				dodgeNeedTick += 4;
			}		
			boolean dodgeIng = mMoveHelper.needDodge(mTick);
			// 当前正在躲避
			if (dodgeIng) {
				int addTick = mMoveHelper.getCurrentDodgeCost();
				dodgeNeedTick += addTick;
				log.debug(String.format("[T%d][HitWarning]%s now in dodgeing,cost will add %d tick", mTick,bullet.toString(),addTick));
			}
			int leftTick = hitTickleft - dodgeNeedTick;

			int dis = Utils.distanceTo(nowX, nowY, bullet.currentX, bullet.currentY);
			log.debug(String.format(
					"[T%d][HitWarning]tankid[%d]bulletDis[%d]hitTickLeft[%d]dodgeNeedTick[%d]tickleft[%d]dodgeDis[%d]->%s--->me[%d,%d]r[%d]dodge[%b]currentBullet[%d]",
					mTick, bullet.tankid, dis, hitTickleft, dodgeNeedTick, leftTick, dodgeDistance, bullet.toString(),
					nowX, nowY, mHeading,dodgeIng,targetUnhandle));

			// 时间到，马上进行闪避
			if (leftTick <= 0) {
				bullet.handled = true;
				mMoveHelper.addDodgeByDistance(dodgeAngle, dodgeDistance);
				log.debug(String.format("[T%d][Dodge]angle[%d]dis[%d]", mTick, dodgeAngle, dodgeDistance));
			} else {
				// 先不用急着闪
				continue;
			}
		}
	}

	// 扫描威胁数据,进行计算
	@Deprecated
	protected void scanThreadTarget() {
		int mTankId = mDatabase.getMyTankId();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();

		Iterator<ThreatTarget> iterator = mThreatTargets.iterator();
		// 是否有新数据
		boolean hasNewData = false;
		while (iterator.hasNext()) {
			ThreatTarget target = iterator.next();
			// 更新tank坐标
			TankGameInfo tankGameInfo = mDatabase.getTankById(target.tankId);
			target.tankX = tankGameInfo.x;
			target.tankY = tankGameInfo.y;
			target.tankHeading = tankGameInfo.r;
			// 已处理过，不再计算
			if (target.handled) {
				int ticketLeft = target.ticketLeft;
				ticketLeft--;
				target.ticketLeft = ticketLeft;
				// todo 是否还有威胁？能打到我或是已超时？更新tick
				if (target.ticketLeft <= 0) {
					iterator.remove();
				} else {
					boolean canHit = Utils.willHit(target.bulletX, target.bulletY, target.bulletR, nowX, nowY,
							AppConfig.TANK_WIDTH);
					if (!canHit) {
						iterator.remove();
					}
				}
				continue;
			}
			hasNewData = true;
			// 计算剩余来袭时间

			// 計算圓心到彈道的垂足 ，p1,p2为弹道 p3圆心
			Position p1 = new Position(target.bulletX, target.bulletY);
			Position p2 = Utils.getNextBulletByTick(target.bulletX, target.bulletY, target.bulletR, 2);
			Position p3 = new Position(nowX, nowY);
			// 垂足
			Position p4 = Utils.getFoot(p1, p2, p3);

			// 坦克半径
			int c = AppConfig.TANK_WIDTH;
			// 垂足到圆心距离
			int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);

			int b = (int) Math.sqrt(c * c - a * a);

			// 总距离
			int dis = Utils.distanceTo(p4.x, p4.y, p1.x, p1.y);
			// 子弹到我的距离
			int distance = dis - b;
			// 剩余来袭时间
			int tick = distance / AppConfig.BULLET_SPEED;

			target.ticketLeft = tick;
			// 计算来袭时间结束

			// 计算最佳躲避方向
			int r = Utils.angleTo(nowX, nowY, p4.x, p4.y);
			target.dodgeBestR = r;
			target.dodgeBestDis = AppConfig.TANK_WIDTH - a;
			target.dodgeBestTick = target.dodgeBestDis / AppConfig.BULLET_SPEED;
		}

		if (mThreatTargets.size() <= 0) {
			log.debug(String.format("tankid[%d]pos[%d,%d] is safe now", mTankId, nowX, nowY));
			return;
		}
		if (!hasNewData) {
			log.debug(String.format("tankid[%d]pos[%d,%d] dont have new threatTarget", mTankId, nowX, nowY));
			return;
		}
		// 按tick升序排序
		Collections.sort(mThreatTargets);

		// 计算最优闪避方向
		List<Integer> angleList = new ArrayList<Integer>();
		int sum = 0;
		// 当前是否正在闪避，如果是取当前方向，否则重新计算
		int currentDodgeR = 0;
		int i = 0;
		Iterator<ThreatTarget> iterator2 = mThreatTargets.iterator();
		while (iterator2.hasNext()) {
			ThreatTarget target = iterator2.next();
			// 已处理过，不再计算
			if (target.handled) {
				continue;
			}
			int angle = target.dodgeBestR;
//			if (i == 0) {
//				//currentDodgeR = angle;
//				angleList.add(angle);
//				continue;
//			}
			angle = (int) Utils.normalAbsoluteAngleDegrees(angle);
			// 获取当前夹角[0,180]
			int span = Utils.bearing(currentDodgeR, angle);
			// 不能为钝角，掉头180度
			if (span > 90) {
				// angle = (int) Utils.normalNearAbsoluteAngleDegrees(angle + 180);
			}
			angleList.add(angle);
			sum += angle;
			i++;

		}
		// 有新威胁数据需要计算，计算闪避方向和时间
		if (angleList.size() > 0) {
			// 应该向哪移动
			int finalR = sum / angleList.size();
			int maxTick = 20;
			// 需要移动多少tick才安全
			int tick = 0;
			Iterator<ThreatTarget> iterator3 = mThreatTargets.iterator();
			while (iterator3.hasNext()) {
				ThreatTarget target = iterator3.next();
				// 已处理过，不再计算
				if (target.handled) {
					continue;
				}
				target.dodgeFinalR = finalR;
				// 一直模拟移动，直到威胁弹道无法命中，最大tick 3*20 = 60
				for (int j = 0; j < maxTick; j++) {
					// todo
					int testTick = j * 3;
					Position newPos = Utils.getNextPostion(nowX, nowY, finalR, testTick);
					boolean willhit = Utils.willHit(target.bulletX, target.bulletY, target.bulletR, newPos.x, newPos.y,
							AppConfig.TANK_WIDTH);
					// 打不到
					if (!willhit) {
						if (testTick > tick) {
							tick = testTick;
						}
						target.handled = true;
						break;
					}
				}
			}

			// 发送躲避数据
			mMoveHelper.addMoveByTick(finalR, tick);
			int tankid = mDatabase.getMyTankId();
			printBulletData();
			printThreatData();
			log.debug(String.format("##Dodge##tank[%d]pos[%d,%d]r[%d]tick[%d]", tankid, nowX, nowY, finalR, tick));
		}
	}

	protected ThreatTarget createThreatTarget(BulletInfo bulletInfo) {
		ThreatTarget target = new ThreatTarget();
		target.bulletInfoId = bulletInfo.getId();
		target.tankId = bulletInfo.getTankId();

		target.bulletX = bulletInfo.getStartX();
		target.bulletY = bulletInfo.getStartY();
		target.bulletR = bulletInfo.getR();
		target.handled = false;
		return target;

	}

	// 打印威胁数据
	protected void printThreatData() {
		log.debug("######ThreatData#########");
		int mTankId = mDatabase.getMyTankId();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int r = mDatabase.getHeading();
		log.debug(String.format("mytankid[%d]pos[%d,%d]r[%d]", mTankId, nowX, nowY, r));
		for (ThreatTarget target : mThreatTargets) {
			log.debug("####" + target.toString() + "##");
		}
	}

	protected void printBulletData() {
		log.debug("#########BulletData#########");
		for (BulletInfo info : mBulletInfos) {
			log.debug(info.toString());
		}
	}
}
