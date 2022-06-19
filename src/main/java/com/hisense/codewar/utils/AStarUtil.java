package com.hisense.codewar.utils;

import java.util.ArrayList;
import java.util.PriorityQueue;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.data.CombatRealTimeDatabase;

/**
 * A星算法工具类
 */
public class AStarUtil {

	CombatRealTimeDatabase mDatabase;
	// 地图 -1 代表墙壁， 1代表起点，2代表终点
	public int[][] map;
	public int R;
	// Open表用优先队列
	public PriorityQueue<Node> Open = new PriorityQueue<Node>();
	// Close表用普通的数组
	public ArrayList<Node> Close = new ArrayList<Node>();
	// Exist表用来存放已经出现过的结点。
	public ArrayList<Node> Exist = new ArrayList<Node>();

	public AStarUtil(CombatRealTimeDatabase databases, int[][] m, int r) {
		this.map = m;
		this.R = r;
		mDatabase = databases;
	}

	public Node astarSearch(Node start, Node end) {
		// 把第一个开始的结点加入到Open表中
		this.Open.add(start);
		// 把出现过的结点加入到Exist表中
		this.Exist.add(start);
		int num = 0;
		// 主循环
		while (Open.size() > 0) {
			Long startTime = System.currentTimeMillis();
			num++;
			// 取优先队列顶部元素并且把这个元素从Open表中删除
			Node current_node = Open.poll();
			// 将这个结点加入到Close表中
			Close.add(current_node);
			// 对当前结点进行扩展，得到一个四周结点的数组
			ArrayList<Node> neighbour_node = extend_current_node(current_node);
			// 对这个结点遍历，看是否有目标结点出现
			// 没有出现目标结点再看是否出现过
			for (Node node : neighbour_node) {
				if (node.x >= end.x - 79 && node.x <= end.x + 79 && node.y >= end.y - 79 && node.y <= end.y + 79) {// 找到目标结点就返回
					// System.out.print("as num : " + num + "\n");
					node.init_node(current_node, end);
					return node;
				}
				if (!is_exist(node)) { // 没出现过的结点加入到Open表中并且设置父节点
					node.init_node(current_node, end);
					Open.add(node);
					Exist.add(node);
				}
			}
			Long nowTime = System.currentTimeMillis();
			if (startTime - nowTime > 20) {
				// System.out.print("Astar coast 20ms\n");
				return null;
			}
		}
		// 如果遍历完所有出现的结点都没有找到最终的结点，返回null
		return null;
	}

	public ArrayList<Node> extend_current_node(Node current_node) {
		int x = current_node.x;
		int y = current_node.y;
		ArrayList<Node> neighbour_node = new ArrayList<Node>();
		if (is_valid(x + 40, y)) {
			Node node = new Node(x + 40, y);
			neighbour_node.add(node);
		}
		if (is_valid(x - 40, y)) {
			Node node = new Node(x - 40, y);
			neighbour_node.add(node);
		}
		if (is_valid(x, y + 40)) {
			Node node = new Node(x, y + 40);
			neighbour_node.add(node);
		}
		if (is_valid(x, y - 40)) {
			Node node = new Node(x, y - 40);
			neighbour_node.add(node);
		}
		return neighbour_node;
	}

	public boolean is_valid(int x, int y) {
		if (x > 1580 || y > 880 || x < 20 || y < 20)
			return false;
		if (map[x + 20][y + 20] == -1 || map[x - 20][y - 20] == -1 || map[x + 20][y - 20] == -1
				|| map[x - 20][y + 20] == -1 || map[x][y + 20] == -1 || map[x][y - 20] == -1 || map[x + 20][y] == -1
				|| map[x - 20][y] == -1)
			return false;
		if ((x >= (800 + R - 40)) || (y >= (0.5625 * (800 + R) - 40)) || (x <= (800 - R + 40))
				|| (y <= (0.5625 * (800 - R) + 40)))
			return false;
		// 如果结点的位置是-1，则不合法

		if (mDatabase.inBlocks(x, y, AppConfig.BLOCK_SIZE * 2)) {
			return false;
		}
		try {
			if (map[x][y] == -1)
				return false;
		} catch (Exception e) {
			System.out.print("is_valid x=" + x + " y= " + y);
		}
		for (Node node : Exist) {
			// 如果结点出现过，不合法
			// if (node.x == x && node.y == y) {
			// return false;
			// }
			if (is_exist(new Node(x, y))) {
				return false;
			}
		}
		// 以上情况都没有则合法
		return true;
	}

	public boolean is_exist(Node node) {
		for (Node exist_node : Exist) {
			if (node.x == exist_node.x && node.y == exist_node.y) {
				return true;
			}
		}
		return false;
	}
}