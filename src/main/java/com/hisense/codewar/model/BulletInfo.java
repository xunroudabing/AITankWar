package com.hisense.codewar.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.utils.Utils;

/**
 * 记录敌人弹道数据 判断是新弹道： 1.tankid无记录 新弹道 2.如果 tankid有记录 但r不同，为新弹道
 * 3.r相同，与弹道记录startX,Y比较，如果等于startX,Y为新弹道
 * 4.不等于startX,Y，带入公式计算是否为同一弹道，如果是则更新currentX,currentY
 * 
 * 游戏服务器计算子弹移动的计算代码如下（c语言）： float rr = a2r(r); int lastx = startx +
 * kProjectileSpeed * cosf(rr) * (elapsed - 1); int lasty = starty +
 * kProjectileSpeed * sinf(rr) * (elapsed - 1); int nowx = startx +
 * kProjectileSpeed * cosf(rr) * elapsed; int nowy = starty + kProjectileSpeed *
 * sinf(rr) * elapsed;
 * 
 * @author hanzheng
 *
 */
public class BulletInfo {
	public static void main(String[] args) {
		// 201,684
		int x = 201;
		int y = 684;

		// 221,565
		int x1 = 221;
		int y1 = 565;
		BulletInfo bulletInfo = new BulletInfo(76, x, y, -80);
		int b = bulletInfo.isChild(x1, y1);

		System.out.println(b);

		// [52]r[168]fire,bullet[650,835]r[168]distance[236]-->result[8]me[418,880]totalSize[3]

		int bx = 650;
		int by = 835;
		int tx = 418;
		int ty = 880;

		boolean hit = willHit(bx, by, 168, tx, ty, 29);
		System.out.println("hit result " + hit);
	}

	private static final Logger log = LoggerFactory.getLogger(BulletInfo.class);
	private List<Position> mBulletRecord;
	private int mTankId;
	private int mStartX;
	private int mStartY;

	private int ticks = 0;
	private int mR = 0;
	private int mCurrentX;
	private int mCurrentY;
	private long mCreateTime = 0L;

	public BulletInfo(int tankid, int startX, int startY, int r) {
		mBulletRecord = new ArrayList<Position>();
		mTankId = tankid;
		mStartX = startX;
		mStartY = startY;
		mCurrentX = mStartX;
		mCurrentY = mStartY;
		ticks = 0;
		mR = r;
		mCreateTime = System.currentTimeMillis();
	}

	public int getTicks() {
		return ticks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public int getStartY() {
		return mStartY;
	}

	public void setStartY(int mStartY) {
		this.mStartY = mStartY;
	}

	public int getStartX() {
		return mStartX;
	}

	public void setStartX(int mStartX) {
		this.mStartX = mStartX;
	}

	public List<Position> getBulletRecord() {
		return mBulletRecord;
	}

	public void setBulletRecord(List<Position> mBulletRecord) {
		this.mBulletRecord = mBulletRecord;
	}

	public int getTankId() {
		return mTankId;
	}

	public int getR() {
		return mR;
	}

	public void setR(int mR) {
		this.mR = mR;
	}

	public int getCurrentY() {
		return mCurrentY;
	}

	public void setCurrentY(int mCurrentY) {
		this.mCurrentY = mCurrentY;
	}

	public int getCurrentX() {
		return mCurrentX;
	}

	public void setCurrentX(int mCurrentX) {
		this.mCurrentX = mCurrentX;
	}

	/**
	 * 子弹存活时间
	 * 
	 * @return
	 */
	public boolean isAlive() {
		long currentTime = System.currentTimeMillis();
		long span = currentTime - mCreateTime;
		return span <= AppConfig.BULLET_ALIVE_TIME;
	}

	/**
	 * 取某个tick时的子弹位置
	 * 
	 * @param tick 从0开始
	 * @return
	 */
	public Position getBulletPositionByTick(int tick) {
		float rr = Utils.a2r(mR);
		// startx + kProjectileSpeed * cosf(rr) * elapsed;
		int x = (int) (mStartX + AppConfig.BULLET_SPEED * Math.cos(rr) * tick);
		// starty + kProjectileSpeed * sinf(rr) * elapsed;
		int y = (int) (mStartY + AppConfig.BULLET_SPEED * Math.sin(rr) * tick);
		return new Position(x, y);
	}

	/**
	 * 获取第N个tick后子弹的位置
	 * 
	 * @param nowX 子弹起始X
	 * @param nowY 子弹起始Y
	 * @param r    角度
	 * @param tick tick>=0
	 * @return
	 */
	public static Position getNextBulletByTick(int nowX, int nowY, int r, int tick) {
		float rr = Utils.a2r(r);
		// startx + kProjectileSpeed * cosf(rr) * elapsed;
		int x = (int) (nowX + AppConfig.BULLET_SPEED * Math.cos(rr) * tick);
		// starty + kProjectileSpeed * sinf(rr) * elapsed;
		int y = (int) (nowY + AppConfig.BULLET_SPEED * Math.sin(rr) * tick);
		return new Position(x, y);
	}

	private static int distanceTo(int x, int y, int tx, int ty) {
		return (int) Math.hypot(x - tx, y - ty);
	}

	/**
	 * 计算当前弹道是否会打到目标 目标是一个以targetwidth为半径的圆，如果垂足长度小于该半径，则会击中目标
	 * 
	 * @param bulletX
	 * @param bulletY
	 * @param r           子弹角度
	 * @param targetX     目标X
	 * @param targetY     目标Y
	 * @param targetWidth 坦克为41X41正方形，此处虚拟为半径29的圆，此处取值建议为29
	 * @return
	 */
	public static boolean willHit(int bulletX, int bulletY, int r, int targetX, int targetY, int targetWidth) {
		Position nextPos = getNextBulletByTick(bulletX, bulletY, r, 1);

		// 计算目标到弹道的垂足
		Position foot = new Position();

		float dx = bulletX - nextPos.x;
		float dy = bulletY - nextPos.y;

		float u = (targetX - bulletX) * dx + (targetY - bulletY) * dy;
		u /= dx * dx + dy * dy;

		foot.x = (int) (bulletX + u * dx);
		foot.y = (int) (bulletY + u * dy);

		int distance = distanceTo(foot.x, foot.y, targetX, targetY);
		log.debug("willhit,distance=" + distance);
		// 垂足小于半径，会打到
		// todo 这里有Bug,还需要判断方向
		return distance < targetWidth;

	}

	/**
	 * 是否归属此弹道的tick index，前提是R相等
	 * 
	 * @param x
	 * @param y
	 * @return -1 - 不归属此弹道 ，大于0，返回所属此弹道的tick index
	 */
	public int isChild(int x, int y) {
		int max = 150;
		for (int i = 0; i < max; i++) {
			Position position = getBulletPositionByTick(i);
			// log.debug(position.toString());
			if (position.x == x && position.y == y) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof BulletInfo) {
			BulletInfo info = (BulletInfo) obj;
			if (mTankId == info.mTankId && mStartX == info.mStartX && mStartY == info.mStartY && mR == info.mR
					&& ticks == info.ticks) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return Objects.hash(mTankId, mStartX, mStartY, mR);
	}
}
