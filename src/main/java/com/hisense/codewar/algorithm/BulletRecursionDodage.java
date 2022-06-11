package com.hisense.codewar.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.utils.Utils;

import javafx.scene.control.Alert;

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
		List<List<Node>> paths = new ArrayList<List<Node>>();
		BulletRecursionDodage recursionDodage = new BulletRecursionDodage(null);
		try {
			long start = System.currentTimeMillis();
			LinkedList<Node> list = recursionDodage.dodage(nowX, nowY, nodes, bullets, 0);
			long end = System.currentTimeMillis();
			long cost = end - start;
			System.out.println("cost " + cost + " ms");

			for (Node node : list) {
				System.out.println(node.toString());
			}

			Node endNode = nodes.getLast();
			for (List<Node> l : paths) {
				log.info("size=" + l.size() + "," + l.toString());
			}
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static final int TANK_DIS = 3;
	public static final int[] DIRECTION = { 0, 90, 180, 270 };
	private static final Logger log = LoggerFactory.getLogger(BulletRecursionDodage.class);
	private CombatRealTimeDatabase mDatabase;

	public BulletRecursionDodage(CombatRealTimeDatabase database) {
		mDatabase = database;
	}

	public LinkedList<Node> dodage(int nowX, int nowY, LinkedList<Node> nodes, List<Bullet> bullets, int tick)
			throws CloneNotSupportedException {
//		log.info("dodge " + nowX + "," + nowY + " nodes.size=" + nodes.size() + " bullets.size=" + bullets.size()
//				+ ",tick=" + tick);

		if (!isValid(nowX, nowY)) {
			Node node = new Node(nowX, nowY, false);
			nodes.addLast(node);
			return nodes;
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
				Node node = new Node(nowX, nowY, false);
				nodes.addLast(node);
				return nodes;
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
		// 该节点有效
		Node node = new Node(nowX, nowY, true);
		// 已躲避完所有子弹，终止递归
		if (!existDangerBullets || bullets.size() <= 0) {
			nodes.addLast(node);
			return nodes;
		} else {
			nodes.addLast(node);
		}
		//
		int t = nodes.size() / 3;
		LinkedList<Node> min_paths = null;
		// 继续躲避子弹
		for (int i = 0; i < DIRECTION.length; i++) {
			int r = DIRECTION[i];
			// 下一个移动位置
			Position nextPosition = getNextTankPositionByDistance(nowX, nowY, r, TANK_DIS);
			// log.info("nextPos=" + nextPosition.toString());
			// 节点已存在，不处理
			if (existNode(nodes, nextPosition.x, nextPosition.y)) {
				continue;
			}

			// 递归
			LinkedList<Node> result = dodage(nextPosition.x, nextPosition.y, nodes, bullets, t);
			Node lastNode = result.getLast();
			if (lastNode.b) {
				// 找一个最短路径
				if (min_paths == null) {
					min_paths = cloneNodes(result);
				} else if (nodes.size() < min_paths.size()) {
					min_paths = cloneNodes(result);
				}
				break;

			} else {
				nodes = popNode(nodes, nowX, nowY);
			}
		}
		// 无路可走
		if (min_paths == null) {
			Node n = nodes.getLast();
			n.b = false;
			return nodes;
		}
		// log.info("min_paths is " + min_paths);
		return min_paths;

	}

	List<Bullet> cloneBullets(List<Bullet> bullets) throws CloneNotSupportedException {
		List<Bullet> list = new ArrayList<Bullet>();
		for (Bullet bullet : bullets) {
			list.add((Bullet) bullet.clone());
		}
		return list;
	}

	LinkedList<Node> cloneNodes(LinkedList<Node> nodes) throws CloneNotSupportedException {
		LinkedList<Node> list = new LinkedList<Node>();
		for (Node node : nodes) {
			list.add((Node) node.clone());
		}
		return list;
	}

	protected LinkedList<Node> popNode(LinkedList<Node> nodes, int nowX, int nowY) {

		boolean remove = false;
		Iterator<Node> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			Node node = (Node) iterator.next();
			if (node.x == nowX && node.y == nowY) {
				remove = true;
				continue;
			}
			if (remove) {
				iterator.remove();
			}
		}
		return nodes;
	}

	protected boolean existNode(List<Node> nodes, int x, int y) {

		for (Node n : nodes) {
			// 考虑计算误差，小于2认为是同一个点
			if (Math.abs(n.x - x) <= 2 && Math.abs(n.y - y) <= 2) {
				return true;
			}
		}
		return false;
	}

	// 坐标有效
	protected boolean isValid(int x, int y) {
		if (mDatabase == null) {
			return true;
		}
		int R = mDatabase.getPoisionR();
		int[][] map = mDatabase.getMapNodeArrays();
//		if ((x > (800 + R - 20)) || (y > (0.5625 * (800 + R) - 20)) || (x < (800 - R + 20))
//				|| (y < (0.5625 * (800 - R) + 20))) {
//			// System.out.print("主动进攻路线规划 毒圈避开angel: " + angel + "\n");
//			return false;
//		}
//		boolean inBlocks = mDatabase.inBlocks(x, y);
//		if (inBlocks) {
//			// System.out.print("主动进攻路线规划 block避开angel: " + angel + "\n");
//			return false;
//		}
//		if(mDatabase.isOut(x, y)) {
//			return false;
//		}
//		if (x > 1580 || y > 880 || x < 20 || y < 20) {
//			return false;
//		}

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

	protected Position getNextTankPositionByDistance(int nowX, int nowY, int r, int distance) {
		float rr = Utils.a2r(r);
		int x = (int) (nowX + Math.cos(rr) * distance);
		int y = (int) (nowY + Math.sin(rr) * distance);
		return new Position(x, y);
	}
}
