package com.hisense.codewar.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.data.CombatMovementHelper;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.Bullet.DodgeSuggestion;
import com.hisense.codewar.utils.Utils;

public class DodageLvl2Algorithm implements IDodageAlgorithm {
	private int mTick;
	private CombatMovementHelper mMovementHelper;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(DodageLvl2Algorithm.class);

	public DodageLvl2Algorithm(CombatRealTimeDatabase database, CombatMovementHelper helper) {
		// TODO Auto-generated constructor stub
		mDatabase = database;
		mMovementHelper = helper;
	}

	@Override
	public int scan(List<Bullet> bulletList, int nowX, int nowY, int tick) {
		// TODO Auto-generated method stub
		mTick = tick;
		int count = bulletList.size();
		List<Bullet> toDoList = new ArrayList<>();
		Iterator<Bullet> iterator2 = bulletList.iterator();
		while (iterator2.hasNext()) {
			Bullet bullet = (Bullet) iterator2.next();
			if (!bullet.isActive(tick)) {
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
			// 不会击中，忽略
			if (!canHit) {
				continue;
			}

			int b = (int) Math.sqrt(c * c - a * a);

			// 总距离
			int totalDistance = Utils.distanceTo(p4.x, p4.y, p1.x, p1.y);
			// 子弹到我的距离
			int distance = totalDistance - b;
			// 剩余来袭时间
			int hitTickleft = distance / AppConfig.BULLET_SPEED;

			// 计算最佳躲避方向，按最佳方向闪避,闪避耗时约为10tick
			int dodgeBestAngle = Utils.angleTo(p4.x, p4.y, nowX, nowY);
			// 所需移动距离
			int dodgeBestDistance = AppConfig.TANK_WIDTH - a;
			// 闪避所需时间 dodgeDistance / AppConfig.TANK_SPEED;
			int dodgeBestNeedTick = Utils.getTicks(dodgeBestDistance, AppConfig.TANK_SPEED);
			// 行动倒计时
			int timer = hitTickleft - dodgeBestNeedTick;
			boolean dodgeIng = mMovementHelper.needDodge(tick);
			// 闪避建议
			DodgeSuggestion suggestion = new DodgeSuggestion();
			suggestion.distance = distance;
			suggestion.dodgeBestAngle = dodgeBestAngle;
			suggestion.dodgeBestDistance = dodgeBestDistance;
			suggestion.dodgeNeedTick = dodgeBestDistance;
			suggestion.hitTickleft = hitTickleft;
			suggestion.dodgeNeedTick = dodgeBestNeedTick;
			suggestion.coutdownTimer = timer;

			bullet.suggestion = suggestion;

			if (timer <= 0) {
				// log.debug(String.format("[Danger]%s not time left", bullet.toString()));
			}

			toDoList.add(bullet);
			int dis = Utils.distanceTo(nowX, nowY, bullet.currentX, bullet.currentY);
			log.debug(String.format(
					"[T%d][HitWarning]tankid[%d]bulletDis[%d]hitTickLeft[%d]dodgeNeedTick[%d]timer[%d]dodgeDis[%d]dodgeAngle[%d]->%s--->me[%d,%d]r[%d]dodge[%b]currentBullet[%d]",
					tick, bullet.tankid, dis, hitTickleft, dodgeBestNeedTick, timer, dodgeBestDistance, dodgeBestAngle,
					bullet.toString(), nowX, nowY, mDatabase.getHeading(), dodgeIng, count));
		}

		// 排序,hitTickleft升序排列，小的在前
		Collections.sort(toDoList, new Comparator<Bullet>() {

			@Override
			public int compare(Bullet o1, Bullet o2) {
				// TODO Auto-generated method stub
				return o1.suggestion.hitTickleft - o2.suggestion.hitTickleft;
			}
		});

		int bulletCount = toDoList.size();
		// 处理会击中我的子弹
		Iterator<Bullet> iterator3 = toDoList.iterator();
//		while (iterator3.hasNext()) {
//			Bullet bullet = (Bullet) iterator3.next();
//
//		}
		if (bulletCount == 1) {
			handleBullet1(toDoList.get(0));

		} else if (bulletCount == 2) {
			Bullet bullet1 = toDoList.get(0);
			Bullet bullet2 = toDoList.get(1);
			handleBullet2(bullet1, bullet2, nowX, nowY);
		}
		return toDoList.size();

	}

	protected void handleBullet1(Bullet bullet) {
		// 时间到，马上进行闪避
		if (bullet.suggestion.coutdownTimer <= AppConfig.DODGE_TICK) {
			bullet.handled = true;
			mMovementHelper.addDodgeByDistance(bullet.suggestion.dodgeBestAngle, bullet.suggestion.dodgeBestDistance);
			log.debug(String.format("[T%d][DodgeAI][1-PlanB]angle[%d]dis[%d]", mTick, bullet.suggestion.dodgeBestAngle,
					bullet.suggestion.dodgeBestDistance));
		}
	}

	protected void handleBullet2(Bullet bullet1, Bullet bullet2, int nowX, int nowY) {
		int tankWith = AppConfig.TANK_WIDTH;
		// 取2弹道夹角 相对值[0,180]
		int angleBullet = Utils.bearing(bullet1.r, bullet2.r);

		// bullet1弹道
		Position line1p1 = new Position(bullet1.startX, bullet1.startY);
		Position line1p2 = Utils.getNextBulletByTick(bullet1.startX, bullet1.startY, bullet1.r, 2);
		// bullet2弹道
		Position line2p1 = new Position(bullet2.startX, bullet2.startY);
		Position line2p2 = Utils.getNextBulletByTick(bullet2.startX, bullet2.startY, bullet2.r, 2);
		// 交点
		Position crossPos = Utils.crossPoint(line1p1, line1p2, line2p1, line2p2);
		// 二线平行
		if (crossPos == null) {

		} else {
			if (angleBullet > 90) {
				log.debug("11111111111111111111111111111111111111");
			}
			// 有交点
			// 從交點到移動到內切圓圓心的距離
			int A = angleBullet / 2;
			int B = 90 - A;
			int disA = (int) (tankWith / Math.sin(Utils.a2r(A)));
			int disB = (int) (tankWith / Math.sin(Utils.a2r(B)));

			int rA = (bullet1.r + bullet2.r) / 2;
			rA = Utils.formatAngle(rA);
			int rB = (bullet1.r + 180 + bullet2.r) / 2;
			rB = Utils.formatAngle(rB);
			// 内切圆圆心
			Position positionA = Utils.getNextPositionByDistance(crossPos.x, crossPos.y, rA, disA);
			Position positionB = Utils.getNextPositionByDistance(crossPos.x, crossPos.y, rB, disB);

			int suggestDisA = Utils.distanceTo(nowX, nowY, positionA.x, positionA.y);
			int suggestDisB = Utils.distanceTo(nowX, nowY, positionB.x, positionB.y);

			int suggestDis = Math.min(suggestDisA, suggestDisB);
			int suggestNeedTick = Utils.getTicks(suggestDis, AppConfig.TANK_SPEED);
			int suggestAngle = 0;
			if (suggestDisA < suggestDisB) {
				suggestAngle = Utils.angleTo(nowX, nowY, positionA.x, positionA.y);
			} else {
				suggestAngle = Utils.angleTo(nowX, nowY, positionB.x, positionB.y);
			}

			// 和hitlefttick作比较，看能否来的及移动到该位置,来的及就进入内切圆圆心位置
			int hitTickleft = bullet1.suggestion.hitTickleft;
			log.debug(
					String.format("[T%d][DodgeAI][2-PlanA]suggestAngle[%d]suggestDis[%d]suggestNeedTick[%d]hitTick[%d]",
							mTick, suggestAngle, suggestDis, suggestNeedTick, hitTickleft));
			if (hitTickleft >= suggestNeedTick) {
				int countDownTimer = hitTickleft - suggestNeedTick;
				bullet1.suggestion.coutdownTimer = countDownTimer;
				if (countDownTimer <= AppConfig.DODGE_TICK) {
//					mMovementHelper.addDodgeByDistance(bullet1.suggestion.dodgeBestAngle,
//							bullet1.suggestion.dodgeBestDistance);
					mMovementHelper.addDodgeByDistance(suggestAngle, suggestDis);
					log.debug(
							String.format("[T%d][DodgeAI][2-PlanA]angle[%d]dis[%d]", mTick, suggestAngle, suggestDis));
					bullet1.handled = true;
				}

			} else {
				// 来不及躲避,转换角度
				// angleBullet = Math.abs(180 - angleBullet);
				bullet1.handled = true;
				mMovementHelper.addDodgeByDistance(bullet1.suggestion.dodgeBestAngle,
						bullet1.suggestion.dodgeBestDistance);
				log.debug(String.format("[T%d][DodgeAI][2-PlanB]angle[%d]dis[%d]", mTick,
						bullet1.suggestion.dodgeBestAngle, bullet1.suggestion.dodgeBestDistance));
			}

		}

	}

	public boolean canHitMe(Bullet bullet, Position myPosition) {
		// 計算圓心到彈道的垂足 ，p1,p2为弹道 p3圆心
		Position p1 = new Position(bullet.currentX, bullet.currentY);
		Position p2 = Utils.getNextBulletByTick(bullet.currentX, bullet.currentY, bullet.r, 2);
		Position p3 = myPosition;
		// 垂足
		Position p4 = Utils.getFoot(p1, p2, p3);

		// 坦克半径
		int c = AppConfig.TANK_WIDTH;
		// 垂足到圆心距离 半径减去此值就是最小移动距离 按B角度躲避公式： (半径-a)/cosB = 移动距离
		int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);
		// 会被击中
		return a < c;
	}
}
