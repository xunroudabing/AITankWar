package com.hisense.codewar.model;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.utils.Utils;

public class Bullet {
	public String id;
	public int startX;
	public int startY;
	public int currentX;
	public int currentY;
	public int r;
	public int tankid;
	public boolean isActive = true;
	// 创建时间，单位tick
	public int createTick;

	private static final Logger log = LoggerFactory.getLogger(Bullet.class);

	public Bullet() {
		// TODO Auto-generated constructor stub
		id = UUID.randomUUID().toString();
	}
	public boolean isActive(int currentTick) {
		int span = currentTick - createTick;
		return span <= 74;
	}
	public boolean isChild(int x, int y) {
		Position p2 = Utils.getNextPostion(startX, startY, r, 2);
		Position p3 = new Position(x, y);
		Position p4 = Utils.getFoot(new Position(startX, startY), p2, p3);
		return Utils.isNear(p4, p3);
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof Bullet) {
			Bullet o = (Bullet) obj;
			if (id.equals(o.id) && tankid == o.tankid) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
//		StringBuilder sb = new StringBuilder();
//		for (Position p : predictPos) {
//			sb.append(String.format("(%d,%d)", p.x, p.y));
//		}
		return String.format("tankid[%d]->Bullet-[%s][%d,%d]r[%d]t[%d]", tankid, id, startX, startY, r, createTick);
	}
}
