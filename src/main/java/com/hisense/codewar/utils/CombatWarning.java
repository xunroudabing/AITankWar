package com.hisense.codewar.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankMapProjectile;

public class CombatWarning {
	private static final Logger log = LoggerFactory.getLogger(CombatWarning.class);
	private int mid = 0;
	private int nowx, nowy = 0;
	private int heading = 0;
	private Random mRandom = new Random();
	private List<TankGameInfo> mTanks = new ArrayList<TankGameInfo>();

	public CombatWarning() {

	}

	protected TankGameInfo getTank(int id) {
		for (TankGameInfo info : mTanks) {
			if (info.id == id) {
				return info;
			}
		}
		return null;
	}

	public void updateMyPos(int id, int x, int y, int h) {
		mid = id;
		nowx = x;
		nowy = y;
		heading = h;
	}

	public void updateTanksPos(List<TankGameInfo> tanks) {
		mTanks = tanks;

	}

	public Suggestion analyze(List<TankMapProjectile> projectiles) {
		long start = System.currentTimeMillis();

		int enemyId = -1;
		int bulletR = 0;
		int minDistance = -1;
		int threat = -1;
		int mDiretion = 0;

		Map<Integer, Integer> countMap = new HashMap<Integer, Integer>();
		int totalSize = projectiles.size();
		for (TankMapProjectile p : projectiles) {
			if (p.tankid == mid) {
				continue;
			}
			TankGameInfo enemy = getTank(p.tankid);
			if (enemy == null) {
				continue;
			}

			int bulletDistance = Utils.getDistance(p.x, p.y, nowx, nowy);
			// 敌人射击夹角
			int r = Utils.getAngle(enemy.x, enemy.y, p.x, p.y, nowx, nowy);

			if (r < 10 && r >= 0) {
				log.error(String.format(
						"***tankid[%d]r[%d]fire,bullet[%d,%d]r[%d]distance[%d]-->result[%d]me[%d,%d]totalSize[%d]",
						p.tankid, enemy.r, p.x, p.y, p.r, bulletDistance, r, nowx, nowy, totalSize));

				if (!countMap.containsKey(p.tankid)) {
					countMap.put(p.tankid, 1);
				} else {
					int count = countMap.get(p.tankid);
					count++;
					countMap.put(p.tankid, count);
				}

//				if (bulletDistance > 100) {
//					if (minDistance < 0) {
//						minDistance = bulletDistance;
//						enemyId = p.tankid;
//						bulletR = p.r;
//						threat = r;
//						mDiretion = Utils.getTargetRadius(enemy.x, enemy.y, nowx, nowy);
//
//					} else if (bulletDistance < minDistance) {
//						minDistance = bulletDistance;
//						enemyId = p.tankid;
//						bulletR = p.r;
//						threat = r;
//						mDiretion = Utils.getTargetRadius(enemy.x, enemy.y, nowx, nowy);
//						
//					}
//					minDistance = bulletDistance;
//				}
			}

		}
		int maxCount = 0;
		int maxEnemyId = -1;
		Iterator<Entry<Integer, Integer>> iterator = countMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Integer> entry = iterator.next();
			int id = entry.getKey();
			int count = entry.getValue();
			log.debug("=====id:" + id+",count:" + count + "====");
			if (count > maxCount) {
				maxCount = count;
				maxEnemyId = id;
			}
		}

		Suggestion suggestion = new Suggestion();
		suggestion.action = Suggestion.NOTHING;

		if (maxEnemyId >= 0) {
			TankGameInfo enemyInfo = getTank(maxEnemyId);
			int distance = Utils.getDistance(nowx, nowy, enemyInfo.x, enemyInfo.y);
			int totalBulletSize = projectiles.size();
			mDiretion = Utils.getTargetRadius(enemyInfo.x, enemyInfo.y, nowx, nowy);
			log.debug(String.format("!!!!!!!!Danger!!!!!!!! enemy[%d]r[%d]distance[%d]shootCount[%d] -->mDirection[%d],totalBullet[%d]",
					maxEnemyId, enemyInfo.r, distance, maxCount, mDiretion,totalBulletSize));

			suggestion.action = Suggestion.DODGE;

			int seed = mRandom.nextBoolean() ? 1 : -1;
			int b = mRandom.nextInt(5) * 10 + 90;
			// 建议躲避方向，应该向子弹方向左右90度闪避
			int dodgeAngle = Utils.formatAngle(mDiretion + seed * b);
			int suggestHeading = mDiretion;
			suggestion.r = dodgeAngle;
			suggestion.heading = suggestHeading;
		}

		long end = System.currentTimeMillis();
		long cost = end - start;
		// log.debug("analyze cost " + cost + "ms");

		return suggestion;
	}

	public static class Suggestion {

		public static final int NOTHING = 0;
		public static final int DODGE = 1;
		public static final int ATTACK = 2;
		/**
		 * 建议往哪个方向躲
		 */
		public int r;
		public int action;
		public int heading;
		public int enemyId;
	}
}
