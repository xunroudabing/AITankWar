package com.hisense.codewar.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.algorithm.DodageLvl3Algorithm.HitResult;
import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.data.CombatMovementHelper;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.Bullet.DodgeSuggestion;
import com.hisense.codewar.utils.Utils;

public class DodageLvl4Algorithm implements IDodageAlgorithm {
	private int mTick;
	private CombatMovementHelper mMovementHelper;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(DodageLvl4Algorithm.class);

	public DodageLvl4Algorithm(CombatRealTimeDatabase database, CombatMovementHelper helper) {
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

			// 会击中，更新闪避建议
			// 闪避建议
			DodgeSuggestion suggestion = new DodgeSuggestion();
			suggestion.distance = result.distance;
			suggestion.dodgeBestAngle = result.dodgeBestAngle;
			suggestion.dodgeBestDistance = result.dodgeBestDistance;
			suggestion.hitTickleft = result.hitTickleft;
			suggestion.dodgeNeedTick = result.dodgeNeedTick;
			suggestion.coutdownTimer = result.coutdownTimer;
			suggestion.dodgeBestMoveToPosition = result.dodgeBestMoveToPosition;
			bullet.suggestion = suggestion;

			toDoList.add(bullet);
		}
		return 0;
	}

	public boolean dodage(int nowX, int nowY, int moveAngle, List<Bullet> bullets, int tick)
			throws CloneNotSupportedException {
		if (!isValid(nowX, nowY)) {
			return false;
		}
		boolean existDangerBullets = false;
		List<Bullet> list = cloneBullets(bullets);
		Iterator<Bullet> iterator = list.iterator();
		while (iterator.hasNext()) {
			Bullet bullet = (Bullet) iterator.next();
			Bullet currentBullet = bullet.nextBullet(tick);

			// log.info(currentBullet.toString());
			// 存活时间到，移除子弹
			if (currentBullet.leftTick <= 0) {
				iterator.remove();
				continue;
			}
			// 已经打中我
			if (hasHitMe(nowX, nowY, currentBullet)) {
				return false;
			}
			// 当前子弹已无威胁
			else if (isSafe(nowX, nowY, currentBullet)) {
				iterator.remove();
				continue;
			}
			// 当前子弹有威胁
			else if (willHitMe(nowX, nowY, currentBullet)) {
				existDangerBullets = true;
			}
		}

		// 已躲避完所有子弹，终止递归
		if (!existDangerBullets || bullets.size() <= 0) {
			return true;
		}
		
		Bullet bullet = list.get(0);
		
		dodage(nowX, nowY, moveAngle, bullets, tick)

	}

	List<Bullet> cloneBullets(List<Bullet> bullets) throws CloneNotSupportedException {
		List<Bullet> list = new ArrayList<Bullet>();
		for (Bullet bullet : bullets) {
			list.add((Bullet) bullet.clone());
		}
		return list;
	}

	// 坐标有效
	protected boolean isValid(int x, int y) {
		if (mDatabase == null) {
			return true;
		}
		int R = mDatabase.getPoisionR();
		int[][] map = mDatabase.getMapNodeArrays();
//			if ((x > (800 + R - 20)) || (y > (0.5625 * (800 + R) - 20)) || (x < (800 - R + 20))
//					|| (y < (0.5625 * (800 - R) + 20))) {
//				// System.out.print("主动进攻路线规划 毒圈避开angel: " + angel + "\n");
//				return false;
//			}
//			boolean inBlocks = mDatabase.inBlocks(x, y);
//			if (inBlocks) {
//				// System.out.print("主动进攻路线规划 block避开angel: " + angel + "\n");
//				return false;
//			}
//			if(mDatabase.isOut(x, y)) {
//				return false;
//			}
//			if (x > 1580 || y > 880 || x < 20 || y < 20) {
//				return false;
//			}

		if (x > 1580 || y > 880 || x < 20 || y < 20) {
			return false;
		}
		if (map[x + 20][y + 20] == -1 || map[x - 20][y - 20] == -1 || map[x + 20][y - 20] == -1
				|| map[x - 20][y + 20] == -1 || map[x][y + 20] == -1 || map[x][y - 20] == -1 || map[x + 20][y] == -1
				|| map[x - 20][y] == -1) {
			return false;
		}
		if ((x >= (800 + R - 40)) || (y >= (0.5625 * (800 + R) - 40)) || (x <= (800 - R + 40))
				|| (y <= (0.5625 * (800 - R) + 40))) {
			return false;
		}
		boolean inBlocks = mDatabase.inBlocks(x, y);
		if (inBlocks) {
			// System.out.print("主动进攻路线规划 block避开angel: " + angel + "\n");
			return false;
		}
		return true;
	}

	protected boolean willHitMe(int nowX, int nowY, Bullet bullet) throws CloneNotSupportedException {
		// 計算圓心到彈道的垂足 ，p1,p2为弹道 p3圆心
		Position p1 = new Position(bullet.currentX, bullet.currentY);
		Bullet nextBullet = bullet.nextBullet(2);
		Position p2 = new Position(nextBullet.currentX, nextBullet.currentY);
		Position p3 = new Position(nowX, nowY);
		// 垂足
		Position p4 = Utils.getFoot(p1, p2, p3);

		// 垂足到圆心距离，即我与弹道垂足的位置， 半径减去此值就是最小移动距离
		int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);
		// System.out.println("a=" + a);
		// 小于半径会被击中
		if (a < AppConfig.TANK_WIDTH) {
			return true;
		}
		return false;
	}

	// 已经击中我
	protected boolean hasHitMe(int nowX, int nowY, Bullet bullet) {
		int distance = Utils.distanceTo(nowX, nowY, bullet.currentX, bullet.currentY);
		return distance < AppConfig.TANK_WIDTH;
	}

	// 当前子弹已远离
	protected boolean isSafe(int nowX, int nowY, Bullet bullet) throws CloneNotSupportedException {
		// 計算圓心到彈道的垂足 ，p1,p2为弹道 p3圆心
		Position p1 = new Position(bullet.currentX, bullet.currentY);
		Bullet nextBullet = bullet.nextBullet(2);
		Position p2 = new Position(nextBullet.currentX, nextBullet.currentY);
		Position p3 = new Position(nowX, nowY);
		// 垂足
		Position p4 = Utils.getFoot(p1, p2, p3);

		int bulletR = Utils.angleTo(bullet.currentX, bullet.currentY, p4.x, p4.y);

		bulletR = Utils.formatAngle(bulletR);
		if (Math.abs(bulletR - Utils.formatAngle(bullet.r)) > 170) {
			return true;
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
		// System.out.println("angleHit=" + angleHit);
		// 子弹已远离
		if (Math.abs(Utils.formatAngle(angleHit) - Utils.formatAngle(bulletR)) >= 170) {
			return result;
		}
		// 垂足到圆心距离，即我与弹道垂足的位置， 半径减去此值就是最小移动距离
		int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);
		// System.out.println("a=" + a);
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
			// 闪避需要移动到的位置
			Position dodgeBestMoveToPosition = Utils.getNextPositionByDistance(nowX, nowY, dodgeBestAngle,
					dodgeBestDistance);
			// 目前已在目标位置
			if (Utils.isNear(new Position(nowX, nowY), dodgeBestMoveToPosition, 2)) {
				return result;
			}
			// 闪避所需时间 dodgeDistance / AppConfig.TANK_SPEED;
			// int dodgeBestNeedTick = Utils.getTicks(dodgeBestDistance,
			// AppConfig.TANK_SPEED);
			int dodgeBestNeedTick = Utils.getTicks2(nowX, nowY, dodgeBestMoveToPosition.x, dodgeBestMoveToPosition.y,
					AppConfig.TANK_SPEED);
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
