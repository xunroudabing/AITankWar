package com.hisense.codewar.config;

import com.jfinal.kit.PropKit;

public class AppConfig {
	public static final String APPVERSION = "1.0.0.3"; 
	public static int MOVE_CHASE_SPEED = 5;
	public static int MOVE_ESCAPE_SPEED = 3;
	public static int BLOCK_SIZE = 21;
	public static int MAP_WITH = 1600;
	public static int MAP_HEIGHT = 900;
	public static int TANK_SIZE = 50;
	public static int COMBAT_MAX_DISTANCE = 500;
	public static int COMBAT_MIN_DISTANCE = 170;
	public static int WAVE_DB_MAXROWS = 2000;
	/**
	 * 提前闪躲，值越大闪躲的越早
	 */
	public static int DODGE_TICK = 2;
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
	public static int TARGET_RADIUS = 20;

	public static void init() {
		PropKit.use("tank.properties");
		BULLET_SPEED = PropKit.getInt("battle.bulletSpeed");
		TANK_SPEED = PropKit.getInt("battle.tankSpeed");
		TANK_WIDTH = PropKit.getInt("battle.tankRadius");
		BULLET_ALIVE_TIME = PropKit.getInt("battle.bullet.aliveTime");
		FIRE_SPAN = PropKit.getInt("battle.fireSpan");
		RADAR_SCAN_RADIUS = PropKit.getInt("battle.radar.scanRadius");
		TARGET_RADIUS = PropKit.getInt("battle.targetRadius");
		DODGE_TICK = PropKit.getInt("dodge.earlytick");
		COMBAT_MAX_DISTANCE = PropKit.getInt("battle.combatMaxDistance");
		COMBAT_MIN_DISTANCE = PropKit.getInt("battle.combatMinDistance");
		MAP_WITH = PropKit.getInt("battle.range.with");
		MAP_HEIGHT = PropKit.getInt("battle.range.height");
		TANK_SIZE = PropKit.getInt("battle.range.tanksize");
		BLOCK_SIZE = PropKit.getInt("battle.block.size");
		MOVE_CHASE_SPEED = PropKit.getInt("battle.chase.speed");
		MOVE_ESCAPE_SPEED = PropKit.getInt("battle.escape.speed");
		WAVE_DB_MAXROWS = PropKit.getInt("wave.database.rows");
	}
}
