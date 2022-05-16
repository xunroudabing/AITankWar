package com.hisense.codewar.config;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

public class AppConfig {
	/**
	 * 子弹速度
	 */
	public static int BULLET_SPEED = 12;
	public static int TANK_SPEED = 3;
	/**
	 * 开火间隔
	 */
	public static int FIRE_SPAN = 30;

	public static int TANK_WIDTH = 29;
	public static int BULLET_ALIVE_TIME = 2442;
	public static int RADAR_SCAN_RADIUS = 400;
	public static void init() {
		PropKit.use("tank.properties");
		BULLET_SPEED = PropKit.getInt("battle.bulletSpeed");
		TANK_SPEED = PropKit.getInt("battle.tankSpeed");
		TANK_WIDTH = PropKit.getInt("battle.tankRadius");
		BULLET_ALIVE_TIME = PropKit.getInt("battle.bullet.aliveTime");
		FIRE_SPAN = PropKit.getInt("battle.fireSpan");
		RADAR_SCAN_RADIUS = PropKit.getInt("battle.radar.scanRadius");
	}
}
