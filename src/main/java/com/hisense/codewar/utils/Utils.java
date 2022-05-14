package com.hisense.codewar.utils;

public class Utils {

	public static int formatAngle(int angle) {
		int a = angle;
		if (a < 0) {
			a += 360;
		}
		return (a + 360) % 360;
	}

	public static int getDistance(int x, int y, int tx, int ty) {
		int dx = tx - x;
		int dy = ty - y;
		int dist = dx * dx + dy * dy;
		return (int) Math.sqrt(dist);
	}

	public static float a2r(int angle) {
		return angle * 3.14159265358979f / 180.0f;
	}

	public static long r2a(double radian) {
		return Math.round(radian * 180.0f / 3.14159265358979f);
	}

	/**
	 * 获取目标角度
	 * 
	 * @param tx
	 * @param ty
	 * @param nowx
	 * @param nowy
	 * @return
	 */
	public static int getTargetRadius(int tx, int ty, int nowx, int nowy) {
		int ret = 0;
		if (tx == nowx) {
			if (ty > nowy) {
				ret = 90;
			} else {
				ret = 270;
			}
		} else {
			ret = (int) r2a(Math.atan((float) (ty - nowy) / (float) tx - nowx));
		}
		if ((tx < nowx && ty < nowy) || (tx < nowx && ty > nowy)) {
			ret += 180;
		}
		return ret;
	}
	/**
	 * 
	 * @param ax 
	 * @param ay
	 * @param bx
	 * @param by
	 * @param cx
	 * @param cy
	 * @return
	 */
	public static int getAngle(int ax,int ay,int bx,int by,int cx,int cy) {
		
		int c = getDistance(ax, ay, bx, by);
		int b = getDistance(ax, ay, cx, cy);
		
		int a = getDistance(bx, by, cx, cy);
		
		if(b == 0 || c == 0) {
			return -1;
		}
		 double d = Math.acos((b*b+c*c-a*a)/(2.0d*b*c));
		 int angle = (int) Math.toDegrees(d);
		 return angle;
	}

}
