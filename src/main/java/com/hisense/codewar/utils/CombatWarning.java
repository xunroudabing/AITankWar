package com.hisense.codewar.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.TankGameInfo;
import com.hisense.codewar.TankMapProjectile;

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
		for (TankMapProjectile p : projectiles) {
			if (p.tankid == mid) {
				continue;
			}
			TankGameInfo enemy = getTank(p.tankid);
			if (enemy == null) {
				continue;
			}
			int bulletDistance = Utils.getDistance(p.x, p.y, nowx, nowy);
			// 子弹如果射向我，角度应为
			int r = Utils.getTargetRadius(nowx, nowy, p.x, p.y);
			int ret = Math.abs(p.r - r);
			ret = (ret + 180) % 180;
			if (ret < 10 && bulletDistance < 1200) {
				if (minDistance < 0) {
					minDistance = bulletDistance;
					enemyId = p.tankid;
					bulletR = p.r;
					threat = ret;

				} else if (bulletDistance < minDistance) {
					minDistance = bulletDistance;
					enemyId = p.tankid;
					bulletR = p.r;
					threat = ret;
				}
				minDistance = bulletDistance;
				log.error(String.format("***tankid[%d]r[%d]fire,bullet[%d,%d]r[%d]distance[%d]-->result[%d]me[%d,%d]shouldr[%d]",
						p.tankid, enemy.r, p.x, p.y, p.r, bulletDistance, ret, nowx, nowy,r));
			} else {
				log.error(String.format("tankid[%d]r[%d]fire,bullet[%d,%d]r[%d]distance[%d]-->result[%d]me[%d,%d]shouldr[%d]",
						p.tankid, enemy.r, p.x, p.y, p.r, bulletDistance, ret, nowx, nowy,r));
			}
		}

		Suggestion suggestion = new Suggestion();
		suggestion.action = Suggestion.NOTHING;

		if (threat > 0) {
			log.debug(
					String.format("####Danger### enemy[%d]bulletDis[%d]threat[%d] -->", enemyId, minDistance, threat));

			suggestion.action = Suggestion.DODGE;

			int seed = mRandom.nextBoolean() ? 1 : 1;
			// 建议躲避方向，应该向子弹方向左右90度闪避
			int dodgeAngle = Utils.formatAngle(bulletR + seed * 90);
			int suggestHeading = Utils.formatAngle(bulletR + 180);
			suggestion.r = dodgeAngle;
			suggestion.heading = suggestHeading;
		}

		long end = System.currentTimeMillis();
		long cost = end - start;
		log.debug("analyze cost " + cost + "ms");

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
