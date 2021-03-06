package com.hisense.codewar.model;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.utils.Utils;

public class Bullet implements Cloneable{
	public DodgeSuggestion suggestion;
	public String id;
	public int startX;
	public int startY;
	public int currentX;
	public int currentY;
	public int r;
	public int tankid;
	public int dstid;
	public boolean isActive = true;
	public boolean handled = false;
	// 创建时间，单位tick
	public int createTick;
	// 更新时间
	public int updateTick;
	// 剩余存活时间
	public int leftTick;
	// 预测弹道的长度，50tick * 12 = 600m
	private static final int MAX_TICK = 50;
	private static final Logger log = LoggerFactory.getLogger(Bullet.class);

	public Bullet() {
		// TODO Auto-generated constructor stub
		id = UUID.randomUUID().toString();
	}

	public boolean isActive(int currentTick) {
		int timeNoUpdate = currentTick - updateTick;// 数据很久没更新了
		// 2个tick周期没有数据，直接移除掉该数据
		if (timeNoUpdate >= 2) {
			return false;
		}
		int span = currentTick - createTick;
		return span <= 74;
	}
	public Bullet nextBullet(int tick) throws CloneNotSupportedException {
		float rr = Utils.a2r(r);
		
		int x = Utils.doubleToInt((currentX + AppConfig.BULLET_SPEED * Math.cos(rr) * tick));
		int y = Utils.doubleToInt((currentY + AppConfig.BULLET_SPEED * Math.sin(rr) * tick));

		Bullet bullet = (Bullet) clone();
		bullet.currentX = x;
		bullet.currentY = y;
		bullet.leftTick -= tick;
		return bullet;
	}
	public boolean isChild(int x, int y) {
		for (int i = 0; i < MAX_TICK; i++) {
			Position position = Utils.getNextBulletByTick(startX, startY, r, i);
			if (Utils.isNear(x, y, position.x, position.y)) {
				return true;
			}
		}
		return false;
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
	public Object clone() throws CloneNotSupportedException {
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
	public String toString() {
		// TODO Auto-generated method stub
//		StringBuilder sb = new StringBuilder();
//		for (Position p : predictPos) {
//			sb.append(String.format("(%d,%d)", p.x, p.y));
//		}
		String shortId = id.substring(id.length() - 5, id.length());
		String suggeStr = suggestion != null ? suggestion.toString() : "";
		return String.format("tankid[%d]->Bullet-[%s]start[%d,%d]current[%d,%d]r[%d]t[%d]-%s", tankid, shortId, startX,
				startY, currentX, currentY, r, createTick,suggeStr);
	}

	public static class DodgeSuggestion {

		// 子弹到我的距离
		public int distance;
		// 剩余来袭时间
		public int hitTickleft;
		// 此处求躲避方向
		// 计算最佳躲避方向，按最佳方向闪避,闪避耗时约为10tick
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
		//最佳移动到的位置
		public Position dodgeBestMoveToPosition;
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return String.format(
					"-Suggest-bulletToMeDis[%d]hitleft[%d]bestAngle[%d]bestDis[%d]dodageNeedTick[%d]timer[%d]dodgeBestMoveToPosition[%d,%d]",
					distance, hitTickleft, dodgeBestAngle, dodgeBestDistance, dodgeNeedTick, coutdownTimer,dodgeBestMoveToPosition.x,dodgeBestMoveToPosition.y);
		}
	}
}
