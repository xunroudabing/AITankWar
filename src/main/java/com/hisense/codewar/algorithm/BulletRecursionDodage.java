package com.hisense.codewar.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.utils.Utils;

public class BulletRecursionDodage {

	public static void main(String[] args) {
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

		List<Bullet> bullets = new ArrayList<Bullet>();
		bullets.add(bullet1);
		bullets.add(bullet2);
		LinkedList<Node> nodes = new LinkedList<>();
		BulletRecursionDodage recursionDodage = new BulletRecursionDodage();
		try {
			long start = System.currentTimeMillis();
			LinkedList<Node> result = recursionDodage.dodage(nowX, nowY, nodes, bullets, 0);
			long end = System.currentTimeMillis();
			long cost = end - start;
			System.out.println("cost " + cost + " ms");
			Node endNode = result.getLast();
			if (endNode.b) {
				System.out.println("find a way,length=" + result.size());
				for (Node node : result) {
					log.info(node.toString());
				}

			} else {
				System.out.println("no way");
			}
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static final int TANK_DIS = 3;
	public static final int[] DIRECTION = { 0, 90, 180, 270 };
	private static final Logger log = LoggerFactory.getLogger(BulletRecursionDodage.class);

	public LinkedList<Node> dodage(int nowX, int nowY, LinkedList<Node> nodes, List<Bullet> bullets, int tick)
			throws CloneNotSupportedException {
		log.info("dodge " + nowX + "," + nowY + " nodes.size=" + nodes.size() + " bullets.size=" + bullets.size()
				+ ",tick=" + tick);
		if (nodes.size() > 0) {
			Node lastNode = nodes.getLast();
			if (!lastNode.b) {
				return nodes;
			}
		}
		if (!isValid(nowX, nowY)) {
			Node node = new Node(nowX, nowY, false);
			nodes.addLast(node);
			return nodes;
		}

		Iterator<Bullet> iterator = bullets.iterator();
		while (iterator.hasNext()) {
			Bullet bullet = (Bullet) iterator.next();
			Bullet currentBullet = bullet.nextBullet(tick);
			// log.info(currentBullet.toString());
			// 剩余时间
			if (currentBullet.leftTick <= 0) {
				iterator.remove();
				continue;
			}
			if (isHitMe(nowX, nowY, currentBullet)) {
				Node node = new Node(nowX, nowY, false);
				nodes.addLast(node);
				return nodes;
			} else if (isSafe(nowX, nowY, currentBullet)) {
				iterator.remove();
				continue;
			}
		}
		// 该节点有效
		Node node = new Node(nowX, nowY, true);
		// 已躲避完所有子弹，终止递归
		if (bullets.size() <= 0) {
			nodes.addLast(node);
			return nodes;
		} else {
			nodes.addLast(node);
		}
		// 每走3步算1tick
		int t = nodes.size() / 3;
		LinkedList<Node> min_paths = null;
		// 继续躲避子弹
		for (int i = 0; i < DIRECTION.length; i++) {
			int r = DIRECTION[i];
			// 下一个移动位置
			Position nextPosition = getNextTankPositionByDistance(nowX, nowY, r, TANK_DIS);
			// 递归
			LinkedList<Node> result = dodage(nextPosition.x, nextPosition.y, nodes, bullets, t);
			if (result == null) {
				continue;
			}
			Node lastNode = result.getLast();
			// 有效路径
			if (lastNode.b) {
				// 找一个最短路径
				if (min_paths == null) {
					min_paths = result;
				} else if (result.size() < min_paths.size()) {
					min_paths = result;
				}

			}
		}
		// 无路可走
		if (min_paths == null) {
			Node end = new Node(nowX, nowY, false);
			nodes.addLast(end);
			return nodes;
		}
		return min_paths;

	}

	// 坐标有效
	protected boolean isValid(int x, int y) {
		return true;
	}

	// 会击中我
	protected boolean isHitMe(int nowX, int nowY, Bullet bullet) {
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

	protected Position getNextTankPositionByDistance(int nowX, int nowY, int r, int distance) {
		float rr = Utils.a2r(r);
		int x = (int) (nowX + Math.cos(rr) * distance);
		int y = (int) (nowY + Math.sin(rr) * distance);
		return new Position(x, y);
	}
}
