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
import com.hisense.codewar.data.CombatMovementHelper.MoveEvent;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.Bullet.DodgeSuggestion;
import com.hisense.codewar.utils.Utils;

public class DodageLvl4Algorithm implements IDodageAlgorithm {
	public static void main(String[] args) throws CloneNotSupportedException {
		Bullet bullet1 = new Bullet();
		bullet1.startX = 100;
		bullet1.startY = 100;
		bullet1.currentX = 100;
		bullet1.currentY = 100;
		bullet1.r = 90;
		bullet1.leftTick = 33;

		Bullet bullet2 = new Bullet();
		bullet2.startX = 300;
		bullet2.startY = 150;
		bullet2.currentX = 300;
		bullet2.currentY = 150;
		bullet2.r = 180;
		bullet2.leftTick = 33;

		int nowX = 100;
		int nowY = 150;

		List<Bullet> list = new ArrayList<>();
		list.add(bullet1);
		list.add(bullet2);

		DodageLvl4Algorithm algorithm = new DodageLvl4Algorithm(null, null);
		Position position = algorithm.dodage(nowX, nowY, list);

		System.out.print(position.toString());
	}

	private int mTick;
	private CombatMovementHelper mMovementHelper;
	private CombatRealTimeDatabase mDatabase;
	public static final int[] DIRECTION = { 0, 180, 270, 90 };
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
			// ????????????????????????????????????????????????
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
			// ????????????
			if (!result.willHitMe) {
				continue;
			}

			// ??????????????????????????????
			// ????????????
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

		// ??????,hitTickleft???????????????????????????
		Collections.sort(toDoList, new Comparator<Bullet>() {

			@Override
			public int compare(Bullet o1, Bullet o2) {
				// TODO Auto-generated method stub
				return o1.suggestion.hitTickleft - o2.suggestion.hitTickleft;
			}
		});

		mDatabase.setToDoList(toDoList);
		try {
			Position position = dodage(nowX, nowY, toDoList);
			if (position != null) {
				int heading = Utils.angleTo(nowX, nowY, position.x, position.y);
				MoveEvent event = new MoveEvent();
				event.startX = nowX;
				event.startY = nowY;
				event.dstX = position.x;
				event.dstY = position.y;
				event.heading = heading;

				mMovementHelper.addDodge(event);
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}
		
		return 0;
	}

	public Position dodage(int nowX, int nowY, List<Bullet> bullets) throws CloneNotSupportedException {
		List<Position> list = new ArrayList<>();
		int size = bullets.size();
		for (int i = 0; i < size; i++) {

			Bullet bullet = bullets.get(i);
			int line1_r = bullet.r + 90;
			int line2_r = bullet.r - 90;

			Position line1_p1 = Utils.getNextPositionByDistance(bullet.startX, bullet.startY, line1_r,
					AppConfig.TANK_WIDTH);
			Position line2_p1 = Utils.getNextPositionByDistance(bullet.startX, bullet.startY, line2_r,
					AppConfig.TANK_WIDTH);

			Position line1_p2 = Utils.getNextPositionByDistance(line1_p1.x, line1_p1.y, bullet.r, 9);
			Position line2_p2 = Utils.getNextPositionByDistance(line2_p1.x, line2_p1.y, bullet.r, 9);
			Position mPosition = new Position(nowX, nowY);
			Position line1_cp = Utils.getFoot(line1_p1, line1_p2, mPosition);
			Position line2_cp = Utils.getFoot(line2_p1, line2_p2, mPosition);
			list.add(line1_cp);
			list.add(line2_cp);

			int n = i + 1;
			if (n >= size) {
				break;
			}
			for (int j = i + 1; j < size; j++) {
				Bullet bullet2 = bullets.get(j);
				int line3_r = bullet2.r + 90;
				int line4_r = bullet2.r - 90;

				Position line3_p1 = Utils.getNextPositionByDistance(bullet2.startX, bullet2.startY, line3_r,
						AppConfig.TANK_WIDTH);
				Position line4_p1 = Utils.getNextPositionByDistance(bullet2.startX, bullet2.startY, line4_r,
						AppConfig.TANK_WIDTH);

				Position line3_p2 = Utils.getNextPositionByDistance(line3_p1.x, line3_p1.y, bullet2.r, 9);
				Position line4_p2 = Utils.getNextPositionByDistance(line4_p1.x, line4_p1.y, bullet2.r, 9);

				Position safe_cp1 = Utils.crossPoint(line1_p1, line1_p2, line3_p1, line3_p2);
				Position safe_cp2 = Utils.crossPoint(line1_p1, line1_p2, line4_p1, line4_p2);

				list.add(safe_cp1);
				list.add(safe_cp2);

			}
		}
		int minDis = Integer.MAX_VALUE;
		int minHitCount = Integer.MAX_VALUE;
		int minHitDis = Integer.MAX_VALUE;
		Position bestPosition = null;
		Position betterPosition = null;
		for (Position position : list) {
			System.out.println("list:" + position.toString());
			if (!isValid(position.x, position.y)) {
				continue;
			}
			boolean isSafe = true;
			int hitCount = 0;
			for (Bullet bullet : bullets) {
				boolean willHitme = willHitMe(position.x, position.y, bullet);
				if (willHitme) {
					isSafe = false;
					hitCount++;
				}
			}
			// ??????
			if (isSafe) {
				int dis = Utils.distanceTo(nowX, nowY, position.x, position.y);
				// ???????????????
				if (dis < minDis) {
					bestPosition = position;
					minDis = dis;
				}
			}
			// ?????????
			else {
				// ?????????????????????
				int dis = Utils.distanceTo(nowX, nowY, position.x, position.y);
				if (hitCount <= minHitCount && dis <= minHitDis) {
					minHitCount = hitCount;
					minHitDis = dis;
					betterPosition = position;
				}
			}

		}

		if (bestPosition == null) {
			return betterPosition;
		}
		return bestPosition;

	}

	public boolean dodage(int nowX, int nowY, List<Bullet> bullets, int tick) throws CloneNotSupportedException {
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
			// ??????????????????????????????
			if (currentBullet.leftTick <= 0) {
				iterator.remove();
				continue;
			}
			// ???????????????
			if (hasHitMe(nowX, nowY, currentBullet)) {
				return false;
			}
			// ????????????????????????
			else if (isSafe(nowX, nowY, currentBullet)) {
				iterator.remove();
				continue;
			}
			// ?????????????????????
			else if (willHitMe(nowX, nowY, currentBullet)) {
				existDangerBullets = true;
			}
		}

		// ???????????????????????????????????????
		if (!existDangerBullets || bullets.size() <= 0) {
			return true;
		}

		Bullet bullet = list.get(0);
		for (int i = 0; i < DIRECTION.length; i++) {
			int move = bullet.suggestion.dodgeBestAngle + DIRECTION[i];
			int t = tick + 1;
			Position nextPostion = Utils.getNextTankPostion(nowX, nowY, move, 1);
			boolean ret = dodage(nextPostion.x, nextPostion.y, bullets, t);
			if (ret) {
				bullet.suggestion.dodgeBestAngle = move;
				return true;
			}
		}

		return false;
	}

	List<Bullet> cloneBullets(List<Bullet> bullets) throws CloneNotSupportedException {
		List<Bullet> list = new ArrayList<Bullet>();
		for (Bullet bullet : bullets) {
			list.add((Bullet) bullet.clone());
		}
		return list;
	}

	// ????????????
	protected boolean isValid(int x, int y) {
		if (mDatabase == null) {
			return true;
		}
		int R = mDatabase.getPoisionR();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		if (x > 1580 || y > 880 || x < 20 || y < 20) {
			return false;
		}

		if (mDatabase.isNearBorderCantMove(x, y)) {
			return false;
		}
		if (mDatabase.isNextPointCantReach(nowX, nowY, x, y)) {
			return false;
		}
		boolean inBlocks = mDatabase.inBlocks(x, y);
		if (inBlocks) {
			return false;
		}
		return true;
	}

	protected boolean willHitMe(int nowX, int nowY, Bullet bullet) throws CloneNotSupportedException {
		// ?????????????????????????????? ???p1,p2????????? p3??????
		Position p1 = new Position(bullet.currentX, bullet.currentY);
		Bullet nextBullet = bullet.nextBullet(2);
		Position p2 = new Position(nextBullet.currentX, nextBullet.currentY);
		Position p3 = new Position(nowX, nowY);
		// ??????
		Position p4 = Utils.getFoot(p1, p2, p3);

		// ????????????????????????????????????????????????????????? ??????????????????????????????????????????
		int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);

		int bulletR = Utils.angleTo(bullet.currentX, bullet.currentY, p4.x, p4.y);

		bulletR = Utils.formatAngle(bulletR);
		// ??????
		if (Math.abs(bulletR - Utils.formatAngle(bullet.r)) > 170) {
			return false;
		}

		// System.out.println("a=" + a);
		// ????????????????????????
		if (a < AppConfig.TANK_WIDTH - 2) {
			return true;
		}
		return false;
	}

	// ???????????????
	protected boolean hasHitMe(int nowX, int nowY, Bullet bullet) {
		int distance = Utils.distanceTo(nowX, nowY, bullet.currentX, bullet.currentY);
		return distance < AppConfig.TANK_WIDTH;
	}

	// ?????????????????????
	protected boolean isSafe(int nowX, int nowY, Bullet bullet) throws CloneNotSupportedException {
		// ?????????????????????????????? ???p1,p2????????? p3??????
		Position p1 = new Position(bullet.currentX, bullet.currentY);
		Bullet nextBullet = bullet.nextBullet(2);
		Position p2 = new Position(nextBullet.currentX, nextBullet.currentY);
		Position p3 = new Position(nowX, nowY);
		// ??????
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
		// ?????????????????????????????? ???p1,p2????????? p3??????
		Position p1 = new Position(bulletX, bulletY);
		Position p2 = Utils.getNextBulletByTick(bulletX, bulletY, bulletR, 2);
		Position p3 = new Position(nowX, nowY);
		// ??????
		Position p4 = Utils.getFoot(p1, p2, p3);

		int angleHit = Utils.angleTo(bulletX, bulletY, p4.x, p4.y);
		// System.out.println("angleHit=" + angleHit);
		// ???????????????
		if (Math.abs(Utils.formatAngle(angleHit) - Utils.formatAngle(bulletR)) >= 170) {
			return result;
		}
		// ????????????????????????????????????????????????????????? ??????????????????????????????????????????
		int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);
		// System.out.println("a=" + a);
		// ????????????????????????
		if (a < AppConfig.TANK_WIDTH - 2) {

			int b = (int) Math.sqrt(AppConfig.TANK_WIDTH * AppConfig.TANK_WIDTH - a * a);
			// ????????????????????????????????????
			int totalDistance = Utils.distanceTo(p4.x, p4.y, p1.x, p1.y);
			// ?????????????????????????????????????????????????????????????????????????????? =
			int distance = totalDistance - b;
			// ??????????????????????????????????????????????????????
			int hitTickleft = distance / AppConfig.BULLET_SPEED;

			// ????????????????????????
			int dodgeBestAngle = Utils.angleTo(p4.x, p4.y, nowX, nowY);
			// ??????????????????
			int dodgeBestDistance = AppConfig.TANK_WIDTH - a;
			// ??????????????????????????????
			Position dodgeBestMoveToPosition = Utils.getNextPositionByDistance(nowX, nowY, dodgeBestAngle,
					dodgeBestDistance);
			// ????????????????????????
			if (Utils.isNear(new Position(nowX, nowY), dodgeBestMoveToPosition, 2)) {
				return result;
			}
			// ?????????????????? dodgeDistance / AppConfig.TANK_SPEED;
			// int dodgeBestNeedTick = Utils.getTicks(dodgeBestDistance,
			// AppConfig.TANK_SPEED);
			int dodgeBestNeedTick = Utils.getTicks2(nowX, nowY, dodgeBestMoveToPosition.x, dodgeBestMoveToPosition.y,
					AppConfig.TANK_SPEED);
			// ???????????????
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

		// ?????????????????????
		public int distance;
		// ??????????????????
		public int hitTickleft;
		// ?????????????????????
		// ????????????????????????????????????????????????,????????????????????????????????????????????????
		// int dodgeAngle = Utils.getTargetRadius(p4.x, p4.y, nowX, nowY);
		public int dodgeBestAngle;
		// ??????????????????
		public int dodgeSuggetAngle;
		public int dodgeSuggestDistance;
		// ??????????????????
		public int dodgeBestDistance;
		// ?????????????????? dodgeDistance / AppConfig.TANK_SPEED;
		public int dodgeNeedTick;
		// ??????????????? ?????????????????????????????????????????????hitTickleft - dodgeNeedTick
		public int coutdownTimer;
		// ????????????????????????
		public Position dodgeBestMoveToPosition;
	}

}
