package com.hisense.codewar.data;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hisense.codewar.model.Bullet;

public class CombatFriendBulletDatabase {
	List<Bullet> mBullets;
	private static volatile CombatFriendBulletDatabase instance;

	private CombatFriendBulletDatabase() {
		mBullets = new CopyOnWriteArrayList<Bullet>();
	}

	public void reset() {
		mBullets.clear();
	}

	public static CombatFriendBulletDatabase getInstance() {
		if (instance == null) {
			synchronized (CombatFriendBulletDatabase.class) {
				if (instance == null) {
					instance = new CombatFriendBulletDatabase();
				}
			}
		}
		return instance;
	}

	public void createBullet(int myid, int dstid, int startx, int starty, int fireangle) {
		Bullet bullet = new Bullet();
		bullet.tankid = myid;
		bullet.dstid = dstid;
		bullet.startX = startx;
		bullet.startY = starty;
		bullet.r = fireangle;
		createBullet(bullet);
	}

	public void createBullet(Bullet bullet) {
		for (Bullet b : mBullets) {
			if (b.tankid == bullet.tankid) {
				mBullets.remove(b);
				break;
			}
		}
		mBullets.add(bullet);
	}

	public boolean exist(int dstid, int tick, int myId) {
		for (Bullet b : mBullets) {
			// 存在队友向目标射击的子弹
			if (b.dstid == dstid && b.tankid != myId) {
				int span = tick - b.createTick;
				if (span > 0 && span < 33) {
					return true;
				}
			}
		}
		return false;
	}
}
