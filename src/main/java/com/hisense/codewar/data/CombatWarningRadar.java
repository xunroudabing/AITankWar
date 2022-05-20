package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.Bullet.DodgeSuggestion;
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
	
	public static void main(String[] args) {
		
		List<Integer> aList = Arrays.asList(90,270);
		int s = getSuggestDodageAngle(aList);
		System.out.print(s);
	}
	
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
		mTick = 0;
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
			// bullet出界了也要考虑
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

	// 扫描威胁弹道
	protected void scanThreatBullet() {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int mHeading = mDatabase.getHeading();
		int targetUnhandle = 0;// 未处理的且会击中我的目标
		int lastDodgeCost = 0;// 上一个目标闪避耗时

		// 闪避总耗时，躲闪总共需要多少时间
		int totalDodageNeedTicks = 0;
		// 最大来袭时间，即最大可用剩余时间，空闲时间
		int maxHitLeftTick = 0;
		// 最小剩余时间 剩余时间过小会来不及处理
		int minLeftTick = -1;
		// 最急需处理的bullet,即最小剩余时间
		String urgentBulletId = null;
		int totalDodgeAngle = 0;
		List<Integer> bestDodgeAngle = new ArrayList<Integer>();
		Iterator<Bullet> iterator2 = mDatabase.getBullets().iterator();
		while (iterator2.hasNext()) {
			Bullet bullet = (Bullet) iterator2.next();
			if (!bullet.isActive(mTick)) {
				iterator2.remove();
			}
			if (bullet.handled) {
				int distance = Utils.distanceTo(nowX, nowY, bullet.currentX, bullet.currentY);
				log.debug(
						String.format("[HitWarning]%s distance[%d] is handled,continue", bullet.toString(), distance));
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
			// 垂足到圆心距离 半径减去此值就是最小移动距离 按B角度躲避公式： (半径-a)/cosB = 移动距离
			int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);
			// 会被击中
			if (a < c) {
				canHit = true;
			}

			if (!canHit) {
				continue;
			}
			// 雷达当前威胁目标+1
			targetUnhandle++;
			int b = (int) Math.sqrt(c * c - a * a);

			// 总距离
			int totalDistance = Utils.distanceTo(p4.x, p4.y, p1.x, p1.y);
			// 子弹到我的距离
			int distance = totalDistance - b;
			// 剩余来袭时间
			int hitTickleft = distance / AppConfig.BULLET_SPEED;
			if (hitTickleft > maxHitLeftTick) {
				maxHitLeftTick = hitTickleft;
			}
			// 被击中，来不及躲
//			if (hitTickleft <= 0) {
//				log.debug(String.format("[T%d][HitMe]hitTickLeft[%d]%s", mTick, hitTickleft, bullet.toString()));
//				continue;
//			}
			// 此处求躲避方向
			// 计算最佳躲避方向，按最佳方向闪避,闪避耗时约为10tick
			int dodgeAngle = Utils.angleTo(p4.x, p4.y, nowX, nowY);
			bestDodgeAngle.add(dodgeAngle);
			totalDodgeAngle += dodgeAngle;
			// 所需移动距离
			int dodgeDistance = AppConfig.TANK_WIDTH - a;
			// 闪避所需时间 dodgeDistance / AppConfig.TANK_SPEED;
			int dodgeNeedTick = Utils.getTicks(dodgeDistance, AppConfig.TANK_SPEED);
			totalDodageNeedTicks += dodgeNeedTick;
			// 为了保险提前2个tick进行闪避
			// dodgeNeedTick += 2;
			// 如果有大于1个目标需要提前闪避，防止被集火
//			if (targetUnhandle > 1) {
//				dodgeNeedTick += lastDodgeCost;
//			} else {
//				// 为了保险提前2个tick进行闪避
//				dodgeNeedTick += 4;
//			}
			boolean dodgeIng = mMoveHelper.needDodge(mTick);
//			// 当前正在躲避
//			if (dodgeIng) {
//				int addTick = mMoveHelper.getCurrentDodgeCost();
//				dodgeNeedTick += addTick;
//				log.debug(String.format("[T%d][HitWarning]%s now in dodgeing,cost will add %d tick", mTick,
//						bullet.toString(), addTick));
//			}
			int leftTick = hitTickleft - dodgeNeedTick;

			// 求最小值,最需要处理的子弹
			if (minLeftTick < 0) {
				minLeftTick = leftTick;
				urgentBulletId = bullet.id;
			} else {
				if (leftTick < minLeftTick) {
					minLeftTick = leftTick;
					urgentBulletId = bullet.id;
				}
			}

			// 闪避建议
			DodgeSuggestion suggestion = new DodgeSuggestion();
			suggestion.distance = distance;
			suggestion.dodgeBestAngle = dodgeAngle;
			suggestion.dodgeBestDistance = dodgeDistance;
			suggestion.dodgeNeedTick = dodgeNeedTick;
			suggestion.hitTickleft = hitTickleft;
			suggestion.dodgeNeedTick = dodgeNeedTick;
			suggestion.leftTick = leftTick;

			bullet.suggestion = suggestion;

			int dis = Utils.distanceTo(nowX, nowY, bullet.currentX, bullet.currentY);
			log.debug(String.format(
					"[T%d][HitWarning]tankid[%d]bulletDis[%d]hitTickLeft[%d]dodgeNeedTick[%d]tickleft[%d]dodgeDis[%d]dodgeAngle[%d]->%s--->me[%d,%d]r[%d]dodge[%b]currentBullet[%d]",
					mTick, bullet.tankid, dis, hitTickleft, dodgeNeedTick, leftTick, dodgeDistance, dodgeAngle,
					bullet.toString(), nowX, nowY, mHeading, dodgeIng, targetUnhandle));

			// 时间到，马上进行闪避
//			if (leftTick <= 0) {
//				bullet.handled = true;
//				mMoveHelper.addDodgeByDistance(dodgeAngle, dodgeDistance);
//				log.debug(String.format("[T%d][Dodge]angle[%d]dis[%d]", mTick, dodgeAngle, dodgeDistance));
//			} else {
//				// 先不用急着闪
//				continue;
//			}
		}

		if (urgentBulletId == null) {
			return;
		} else if (targetUnhandle <= 0) {
			return;
		}
		
		
		//todo排序？
		
		// 计算建议躲避方向，多个方向取平均值
		//int suggestAngle = totalDodgeAngle / targetUnhandle;
		int suggestAngle = getSuggestDodageAngle(bestDodgeAngle);
		// 是否有足够的时间去闪避
		int span = maxHitLeftTick - totalDodageNeedTicks;
		Iterator<Bullet> iterator3 = mDatabase.getBullets().iterator();
		while (iterator3.hasNext()) {
			Bullet bullet = (Bullet) iterator3.next();
			if (bullet.handled) {
				continue;
			}

			if (bullet.suggestion != null) {
				// 先处理最紧急的
				if (bullet.id.equals(urgentBulletId) && targetUnhandle > 1) {
					int leftTick = bullet.suggestion.leftTick;
					int otherTotalNeedTick = totalDodageNeedTicks - bullet.suggestion.dodgeNeedTick;
					// 将躲避时间提前,给后面的躲避留出时间
					int leftTickEarly = leftTick - otherTotalNeedTick;
					log.debug(String.format("[urgent]leftTick[%d]leftTickEarly[%d]otherTotalNeedTick[%d]", leftTick,
							leftTickEarly, otherTotalNeedTick));
					// 现在需要立刻躲避
					if (leftTickEarly <= 2) {
						bullet.handled = true;
						// 多弹道情况需要再考虑最佳方向 交叉火力需要算多条线的垂线
						int a = (int) Utils.a2r(suggestAngle);
						int suggestDodgeDistance = (int) (bullet.suggestion.dodgeBestDistance / Math.cos(a));
						int suggestTick = Utils.getTicks(suggestDodgeDistance, AppConfig.TANK_SPEED);
						// 时间不够怎么办
						// if(suggestTick > bullet.suggestion.hitTickleft)
						mMoveHelper.addDodgeByDistance(suggestAngle, suggestDodgeDistance);

						//
						// mMoveHelper.addDodgeByDistance(bullet.suggestion.dodgeBestAngle,
						// bullet.suggestion.dodgeBestDistance);
						log.debug(String.format(
								"[T%d][Dodge][urgent]suggestAngle[%d]suggestDis[%d]earlyTick[%d]bestAngle[%d]bestDis[%d]", mTick,
								suggestAngle, suggestDodgeDistance, leftTickEarly, bullet.suggestion.dodgeBestAngle,
								bullet.suggestion.dodgeBestDistance));
					}
				} else {
					// 时间到，马上进行闪避
					if (bullet.suggestion.leftTick <= 2) {
						bullet.handled = true;
						mMoveHelper.addDodgeByDistance(bullet.suggestion.dodgeBestAngle,
								bullet.suggestion.dodgeBestDistance);
						log.debug(String.format("[T%d][Dodge]angle[%d]dis[%d]", mTick, bullet.suggestion.dodgeBestAngle,
								bullet.suggestion.dodgeBestDistance));
					}

				}

			}
		}
	}

	public static int getSuggestDodageAngle(List<Integer> list) {
		int lastAngle = 0;
		int ret = 0;
		for (int i = 0; i < list.size(); i++) {
			int angle = list.get(i);
			if (i == 0) {
				lastAngle = angle;
				ret = angle;
			} else {
				// 与上次结果求夹角
				int inAngle = Math.abs(angle - lastAngle);
				// 还是采用bestAngle
				if (inAngle == 0 || inAngle == 180) {
					ret = angle;
				}
				// 锐角，求平均值
				else if (inAngle <= 90) {
					ret = (angle + lastAngle) / 2;
					lastAngle = ret;
				} else {
					// 如果是钝角，转成锐角
					ret = (lastAngle + 180 + lastAngle) / 2;
					ret = Utils.formatAngle(ret);
					lastAngle = ret;
				}
			}
		}
		return ret;
	}
}
