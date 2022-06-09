package com.hisense.codewar.algorithm;

import com.hisense.codewar.utils.Utils;

public class Bullet implements Cloneable {
	public static final int BULLET_SPEED = 12;
	public String id;
	public int startX;
	public int startY;
	public int currentX;
	public int currentY;
	public int r;
	// 剩余存活时间
	public int leftTick;

	public Bullet nextBullet(int tick) throws CloneNotSupportedException {
		float rr = Utils.a2r(r);

		int x = (int) (currentX + BULLET_SPEED * Math.cos(rr) * tick);
		int y = (int) (currentY + BULLET_SPEED * Math.sin(rr) * tick);

		Bullet bullet = (Bullet) clone();
		bullet.currentX = x;
		bullet.currentY = y;
		bullet.leftTick -= tick;
		return bullet;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		Bullet ret = (Bullet) super.clone();
		ret.id = this.id;
		ret.startX = this.startX;
		ret.startY = this.startY;
		ret.currentX = this.currentX;
		ret.currentY = this.currentY;
		ret.r = this.r;
		ret.leftTick = this.leftTick;
		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof Bullet) {
			Bullet o = (Bullet) obj;
			if (id.equals(o.id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("[Bullet]id[%s]startPos[%d,%d]currentPos[%d,%d]r[%d]leftTick[%d]", id, startX, startY,
				currentX, currentY, r, leftTick);
	}
}
