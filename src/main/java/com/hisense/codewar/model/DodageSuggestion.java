package com.hisense.codewar.model;

public class DodageSuggestion {
	Bullet bullet;
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
	public int leftTick;
}
