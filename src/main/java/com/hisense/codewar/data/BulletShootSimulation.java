package com.hisense.codewar.data;

import java.util.List;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.utils.Utils;
import com.sun.prism.impl.BufferUtil;

/**
 * 第一颗子弹 关键参数：给出坦克xy，移动方向，耗时tick，和目前子弹位置，方向，预测tick后是否来得及躲避 得出关键结算结果
 * 子弹当前距离（不是到圆心的距离） 击中坦克剩余时间ht1 躲避所需时间dt1 ht1>dt1则可以完成躲避 第二颗子弹 ht2 dt2
 * 
 * @author Administrator
 *
 */
public class BulletShootSimulation {
	public void predict(int nowx, int nowy, int r, Bullet bullet, int tick) {

		Position tanNextPos = Utils.getNextPostion(nowx, nowy, r, tick);

		Position bulletNextPos = Utils.getNextBulletByTick(bullet.currentX, bullet.currentY, bullet.r, tick);
		// if canhit

	}

	public boolean canHit(Bullet bullet, int nowX, int nowY, int tankWith) {
		// 計算圓心到彈道的垂足 ，p1,p2为弹道 p3圆心
		Position p1 = new Position(bullet.currentX, bullet.currentY);
		Position p2 = Utils.getNextBulletByTick(bullet.currentX, bullet.currentY, bullet.r, 2);
		Position p3 = new Position(nowX, nowY);
		// 垂足
		Position p4 = Utils.getFoot(p1, p2, p3);

		// 垂足到圆心距离 半径减去此值就是最小移动距离
		int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);
		return a < tankWith;

	}

	public void canAvoid(Bullet bullet1, Bullet bullet2, int nowX, int nowY, int tankWith) {
		// 計算圓心到彈道的垂足 ，p1,p2为弹道 p3圆心
		Position p1 = new Position(bullet1.currentX, bullet1.currentY);
		Position p2 = Utils.getNextBulletByTick(bullet1.currentX, bullet1.currentY, bullet1.r, 2);
		Position p3 = new Position(nowX, nowY);
		// 垂足
		Position p4 = Utils.getFoot(p1, p2, p3);

		// 垂足到圆心距离 半径减去此值就是最小移动距离 按B角度躲避公式： (半径-a)/cosB = 移动距离
		int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);

		int b = (int) Math.sqrt(tankWith * tankWith - a * a);

		// 总距离
		int totalDistance = Utils.distanceTo(p4.x, p4.y, p1.x, p1.y);
		// 子弹到我的距离
		int distance = totalDistance - b;

		// 剩余来袭时间
		int hitTickleft = distance / AppConfig.BULLET_SPEED;
		// 计算最佳躲避方向，按最佳方向闪避,闪避最大耗时约为10tick
		int dodgeBestAngle = Utils.angleTo(p4.x, p4.y, nowX, nowY);
		// 所需最短移动距离
		int dodgeBestDistance = AppConfig.TANK_WIDTH - a;
		// 闪避所需时间 dodgeDistance / AppConfig.TANK_SPEED;
		int dodgeBestNeedTick = Utils.getTicks(dodgeBestDistance, AppConfig.TANK_SPEED);

		// 取2弹道夹角 相对值[0,180]
		int angleBullet = Utils.bearing(bullet1.r, bullet2.r);

		// bullet1弹道
		Position line1p1 = new Position(bullet1.startX, bullet1.startY);
		Position line1p2 = Utils.getNextBulletByTick(bullet1.startX, bullet1.startY, bullet1.r, 2);
		// bullet2弹道
		Position line2p1 = new Position(bullet2.startX, bullet2.startY);
		Position line2p2 = Utils.getNextBulletByTick(bullet2.startX, bullet2.startY, bullet2.r, 2);
		// 交点
		Position crossPos = Utils.crossPoint(line1p1, line1p2, line2p1, line2p2);
		// 二线平行
		if (crossPos == null) {

		} else {
			// 有交点
			// 從交點到移動到內切圓圓心的距離
			int A = angleBullet / 2;
			int B = 90 - A;
			int disA = (int) (tankWith / Math.sin(Utils.a2r(A)));
			int disB = (int) (tankWith / Math.sin(Utils.a2r(B)));
			
			int rA = (bullet1.r + bullet2.r) / 2;
			rA = Utils.formatAngle(rA);
			int rB = (bullet1.r + 180 + bullet2.r) / 2;
			rB = Utils.formatAngle(rB);
			// 内切圆圆心
			Position positionA = Utils.getNextPositionByDistance(crossPos.x, crossPos.y, rA, disA);
			Position positionB = Utils.getNextPositionByDistance(crossPos.x, crossPos.y, rB, disB);
			
			int suggestDisA = Utils.distanceTo(nowX, nowY, positionA.x, positionA.y);
			
			int suggestDisB = Utils.distanceTo(nowX, nowY, positionB.x, positionB.y);
			
			int suggestDis = Math.min(suggestDisA, suggestDisB);
			int suggestNeedTick = suggestDis / AppConfig.TANK_SPEED;
			// 和hitlefttick作比较，看能否来的及移动到该位置,来的及就进入内切圆圆心位置
			if (hitTickleft >= suggestNeedTick) {
				int countDownTimer = hitTickleft - suggestNeedTick;
			} else {
				// 来不及躲避,转换角度
				angleBullet = Math.abs(180 - angleBullet);
			}
			// 反之走平均值算法

		}

	}

}
