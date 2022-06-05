package com.hisense.codewar.algorithm;

import java.awt.geom.FlatteningPathIterator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale.Category;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.data.CombatMovementHelper;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.utils.Utils;

public class DodageLvl3Algorithm implements IDodageAlgorithm {
	public static void main(String[] args) {
		// 161,668
		int x1 = 161;
		int y1 = 668;
		// 255,686
		int x3 = 255;
		int y3 = 686;

		int r = 11;
		// 231,681
		int x2 = 231;
		int y2 = 681;

		for (int i = 0; i < 15; i++) {
			Position position = Utils.getNextBulletByTick(x1, y1, 11, i);
			System.out.println(position.toString());
		}

		boolean hit = willHitMe(x3, y3, r, x2, y2).willHitMe;
		System.out.println("hit=" + hit);
	}

	private int mTick;
	private CombatMovementHelper mMovementHelper;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(DodageLvl2Algorithm.class);

	public DodageLvl3Algorithm(CombatRealTimeDatabase database, CombatMovementHelper helper) {
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

			HitResult result = willHitMe(bullet.currentX, bullet.currentY, bullet.r, nowX, nowY);
			// 不会击中
			if (!result.willHitMe) {
				continue;
			}

			toDoList.add(bullet);
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
		// 处理会击中我的子弹,目前只处理前2个
		if (bulletCount == 1) {
			//handleBullet1(toDoList.get(0));

		} else if (bulletCount >= 2) {
			Bullet bullet1 = toDoList.get(0);
			Bullet bullet2 = toDoList.get(1);
			//handleBullet2(bullet1, bullet2, nowX, nowY);
		}

		return 0;
	}

	protected boolean simulationWillbeHit(List<Bullet> bulletList, int nowX, int nowY, int r, int distance) {
		Iterator<Bullet> iterator2 = bulletList.iterator();
		while (iterator2.hasNext()) {
			Bullet bullet = (Bullet) iterator2.next();
		}

		int time = 1;
		int maxTime = Utils.getTicks(distance, AppConfig.TANK_SPEED);
		while (time <= maxTime) {
			Position myPosition = Utils.getNextPositionByDistance(nowX, nowY, r, AppConfig.TANK_SPEED);
			for (Bullet bullet : bulletList) {
				Position bulletPos = Utils.getNextBulletByTick(bullet.currentX, bullet.currentY, bullet.r, time);
				int dis = Utils.distanceTo(bulletPos.x, bulletPos.y, myPosition.x, myPosition.y);
				// 被击中
				if (dis < AppConfig.TANK_WIDTH) {
					return true;
				}
				if (time == maxTime) {
					HitResult result = willHitMe(bulletPos.x, bulletPos.y, bullet.r, myPosition.x, myPosition.y);
					if (result.willHitMe) {
						// 来不及躲避
						if (result.coutdownTimer <= 0) {
							return true;
						}
					}

				}

			}
			time++;
		}

		return false;
	}

	public static HitResult willHitMe(int bulletX, int bulletY, int bulletR, int nowX, int nowY) {
		HitResult result = new HitResult();
		result.willHitMe = false;
		// 計算圓心到彈道的垂足 ，p1,p2为弹道 p3圆心
		Position p1 = new Position(bulletX, bulletY);
		Position p2 = Utils.getNextBulletByTick(bulletX, bulletY, bulletR, 2);
		Position p3 = new Position(nowX, nowY);
		// 垂足
		Position p4 = Utils.getFoot(p1, p2, p3);

		int angleHit = Utils.angleTo(bulletX, bulletY, p4.x, p4.y);
		System.out.println("angleHit=" + angleHit);
		// 子弹已远离
		if (Math.abs(angleHit - bulletR) >= 170) {
			return result;
		}
		// 垂足到圆心距离，即我与弹道垂足的位置， 半径减去此值就是最小移动距离
		int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);
		System.out.println("a=" + a);
		// 小于半径会被击中
		if (a < AppConfig.TANK_WIDTH) {

			int b = (int) Math.sqrt(AppConfig.TANK_WIDTH * AppConfig.TANK_WIDTH - a * a);
			// 总距离，子弹到圆心的距离
			int totalDistance = Utils.distanceTo(p4.x, p4.y, p1.x, p1.y);
			// 子弹到我的距离，不是到圆心的距离，而是到弹着点的距离 =
			int distance = totalDistance - b;
			// 剩余来袭时间，直接取整会导致误差较大
			int hitTickleft = distance / AppConfig.BULLET_SPEED;

			// 计算最佳躲避方向
			int dodgeBestAngle = Utils.angleTo(p4.x, p4.y, nowX, nowY);
			// 所需移动距离
			int dodgeBestDistance = AppConfig.TANK_WIDTH - a;
			// 闪避所需时间 dodgeDistance / AppConfig.TANK_SPEED;
			int dodgeBestNeedTick = Utils.getTicks(dodgeBestDistance, AppConfig.TANK_SPEED);
			// 1个action走3米，移动distance需要多少个action，向上取整
			int dodgeActionCount = Utils.getTicks(dodgeBestDistance, 3);
			// 闪避需要移动到的位置
			Position dodgeBestMoveToPosition = Utils.getNextPositionByDistance(nowX, nowY, dodgeBestAngle,
					dodgeBestDistance);
			// 行动倒计时
			int timer = hitTickleft - dodgeBestNeedTick;

			result.willHitMe = true;
			result.distance = distance;
			result.dodgeBestAngle = dodgeBestAngle;
			result.dodgeBestDistance = dodgeBestDistance;
			result.dodgeBestMoveToPosition = dodgeBestMoveToPosition;
			result.coutdownTimer = timer;
		}

		return result;
	}


	public static class HitResult {
		public boolean willHitMe;

		// 子弹到我的距离
		public int distance;
		// 剩余来袭时间
		public int hitTickleft;
		// 此处求躲避方向
		// 计算最佳躲避方向，按最佳方向闪避,闪避耗时最大就是移动半径所耗时间
		// int dodgeAngle = Utils.getTargetRadius(p4.x, p4.y, nowX, nowY);
		public int dodgeBestAngle;
		// 建议躲避方向
		public int dodgeSuggetAngle;
		public int dodgeSuggestDistance;
		// 所需移动距离
		public int dodgeBestDistance;
		// 闪避所需时间 dodgeDistance / AppConfig.TANK_SPEED;
		public int dodgeNeedTick;
		// 倒计时时间 剩余来袭时间减去闪避所需时间，hitTickleft - dodgeNeedTick
		public int coutdownTimer;
		// 最佳移动到的位置
		public Position dodgeBestMoveToPosition;
	}

}
