package com.hisense.codewar.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Position;

public class PoisionCircleUtils {
	static final double X_less = 1d;
	static final double Y_LESS = 0.57d;
	private static final Logger log = LoggerFactory.getLogger(PoisionCircleUtils.class);

	private int minX;
	private int maxX;
	private int minY;
	private int maxY;
	private int radius;

	public PoisionCircleUtils() {
		minX = 0;
		maxX = AppConfig.MAP_WITH;
		minY = 0;
		maxY = AppConfig.MAP_HEIGHT;
	}

	public void reset() {
		minX = 0;
		maxX = AppConfig.MAP_WITH;
		minY = 0;
		maxY = AppConfig.MAP_HEIGHT;
	}

	public void updateR(int r) {
		radius = r;
		int xSize = (AppConfig.MAP_WITH - radius * 2) / 2;
		int ySize = (xSize * 9 / 16);

		minX = xSize;
		maxX = AppConfig.MAP_WITH - xSize;
		minY = ySize;
		maxY = AppConfig.MAP_HEIGHT - ySize;
		log.debug(String.format("[Map][%d,%d-%d,%d-%d,%d-%d,%d]", minX, minY, minX, maxY, maxX, maxY, maxX, minY));
	}

	public boolean isOut(int x, int y) {
		if (x > maxX - AppConfig.TANK_WIDTH || x < minX + AppConfig.TANK_WIDTH) {
			return true;
		} else if (y > maxY - AppConfig.TANK_WIDTH || y < minY + AppConfig.TANK_WIDTH) {
			return true;
		}
		return false;
	}

	public boolean inLeft(int x, int y) {
		if (x <= minX + AppConfig.TANK_SIZE) {
			return true;
		}
		return false;
	}

	public boolean inRight(int x, int y) {
		if (x >= maxX - AppConfig.TANK_SIZE) {
			log.debug(String.format("[inRight][%d,%d][Map][%d,%d-%d,%d-%d,%d-%d,%d]", x, y, minX, minY, minX, maxY,
					maxX, maxY, maxX, minY));
			return true;
		}
		return false;
	}

	public boolean inTop(int x, int y) {
		if (y >= maxY - AppConfig.TANK_WIDTH) {
			return true;
		}
		return false;
	}

	public boolean inBottom(int x, int y) {
		if (y <= minY + AppConfig.TANK_WIDTH) {
			return true;
		}
		return false;
	}

	public static boolean isOutRange(int x, int y) {
		if (x > AppConfig.MAP_WITH - AppConfig.TANK_SIZE || x < AppConfig.TANK_SIZE) {
			return true;
		} else if (y > AppConfig.MAP_HEIGHT - AppConfig.TANK_SIZE || y < AppConfig.TANK_SIZE) {
			return true;
		}
		return false;
	}

	public static boolean isOutRange(int x, int y, int tick) {

		int seed = tick % 15;
		int minX = (int) (0 + seed * X_less / 2);
		int maxX = (int) (AppConfig.MAP_WITH - seed * X_less / 2);

		int minY = (int) (0 + seed * Y_LESS / 2);
		int maxY = (int) (AppConfig.MAP_HEIGHT - seed * Y_LESS / 2);

		if (x > maxX - AppConfig.TANK_SIZE || x < minX + AppConfig.TANK_SIZE) {
			return true;
		} else if (y > maxY - AppConfig.TANK_SIZE || y < minY + AppConfig.TANK_SIZE) {
			return true;
		}
		return false;
	}

	public static Position[] getMap(int tick) {
		int seed = tick % 15;
		int minX = (int) (0 + seed * X_less / 2);
		int maxX = (int) (AppConfig.MAP_WITH - seed * X_less / 2);

		int minY = (int) (0 + seed * Y_LESS / 2);
		int maxY = (int) (AppConfig.MAP_HEIGHT - seed * Y_LESS / 2);

		Position[] array = new Position[4];
		array[0] = new Position(minX, minY);
		array[1] = new Position(minX, maxY);
		array[2] = new Position(maxX, maxY);
		array[3] = new Position(maxX, minY);
		log.debug(String.format("[Map][%d,%d-%d,%d-%d,%d-%d,%d]", minX, minY, minX, maxY, maxX, maxY, maxX, minY));
		return array;
	}

}
