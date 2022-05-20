package com.hisense.codewar.data;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.utils.Utils;

/**
 * 躲避计算
 * 
 * @author Administrator
 *
 */
public class DodageCalculation {

	public boolean canHitMe(Bullet bullet, Position myPosition) {
		// 計算圓心到彈道的垂足 ，p1,p2为弹道 p3圆心
		Position p1 = new Position(bullet.currentX, bullet.currentY);
		Position p2 = Utils.getNextBulletByTick(bullet.currentX, bullet.currentY, bullet.r, 2);
		Position p3 = myPosition;
		// 垂足
		Position p4 = Utils.getFoot(p1, p2, p3);

		// 坦克半径
		int c = AppConfig.TANK_WIDTH;
		// 垂足到圆心距离 半径减去此值就是最小移动距离 按B角度躲避公式： (半径-a)/cosB = 移动距离
		int a = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);
		// 会被击中
		return a < c;
	}
}
