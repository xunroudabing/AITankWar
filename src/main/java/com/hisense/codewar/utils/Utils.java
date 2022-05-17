package com.hisense.codewar.utils;

import static java.lang.Math.PI;

import java.awt.Point;
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Position;

public class Utils {
	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	public static void main(String[] args) {
		// 346,423
		int x = 630;
		int y = 284;
		int r = -4;
		
		int x1 = 631;
		int y1 = 283;

		boolean b = Utils.isNear(x1, y1, x, y);
		System.out.println(b);
		for(int i=0;i<20;i++) {
			Position position = Utils.getNextBulletByTick(x, y, r, i);
			System.out.println(position);
		}
	

	}

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

	/**
	 * π 180° π/2 90° π/4 45° π/6 30° 角度 转成 弧度
	 * 
	 * @param angle
	 * @return 弧度
	 */
	public static float a2r(int angle) {
		return angle * 3.14159265358979f / 180.0f;
	}

	/**
	 * 弧度转成角度 角度 = 弧度 * 180 / PI
	 * 
	 * @param radian 弧度
	 * @return
	 */
	public static int r2a(double radian) {
		// return Math.round(radian * 180.0f / 3.14159265358979f);
		return (int) Math.toDegrees(radian);
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
		// return getFireAngle(nowx, nowy, tx, ty);
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
		// bearing角，[-180~180]
		if ((tx < nowx && ty < nowy) || (tx < nowx && ty > nowy)) {
			ret += 180;
		}
		return ret;
	}

	public static int getFireAngle(int nowx, int nowy, int tx, int ty) {
		int ret = 0;
		if (tx == nowx) {
			if (ty > nowy) {
				ret = 90;
			} else {
				ret = 270;
			}
		} else {
			ret = angleToDegress(nowx, nowy, tx, ty);
		}
		// bearing角，[-180~180]
//		if ((tx < nowx && ty < nowy) || (tx < nowx && ty > nowy)) {
//			ret += 180;
//		}
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
	public static int getAngle(int ax, int ay, int bx, int by, int cx, int cy) {

		int c = getDistance(ax, ay, bx, by);
		int b = getDistance(ax, ay, cx, cy);

		int a = getDistance(bx, by, cx, cy);

		if (b == 0 || c == 0) {
			return -1;
		}
		double d = Math.acos((b * b + c * c - a * a) / (2.0d * b * c));
		log.debug("d=" + Math.toDegrees(d));
		int angle = (int) Math.toDegrees(d);
		return angle;
	}

	public static boolean inline(int x1, int y1, int x2, int y2, int tx, int ty) {
		boolean ret = (tx - x1) * (y1 - y2) == (x1 - x2) * (ty - y1);
		return ret;

	}

	/**
	 * 计算 p3 到 p1,p2的垂足
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return
	 */
	public static Position getFoot(Position p1, Position p2, Position p3) {
		Position foot = new Position();

		float dx = p1.x - p2.x;
		float dy = p1.y - p2.y;

		float u = (p3.x - p1.x) * dx + (p3.y - p1.y) * dy;
		u /= dx * dx + dy * dy;

		foot.x = (int) (p1.x + u * dx);
		foot.y = (int) (p1.y + u * dy);

		return foot;
	}

	// 点到其他两点垂足计算
//    public static Point getFoot(Point pt,Point beginPt,Point endPt){
//        Point result = new Point();
//        double dx = beginPt.getX()-endPt.getX();
//        double dy = beginPt.getY()-endPt.getY();
// 
//        if(Math.abs(dx)<0.000001&&Math.abs(dy)<0.000001){
//            result = pt;
//        }
// 
//        double u = (pt.getX()-beginPt.getX())*(beginPt.getX()-endPt.getX())+
//                (pt.getY()-beginPt.getY())*(beginPt.getY()-endPt.getY());
//        log.debug("u=" + u);
//        u = u /(dx*dx+dy*dy);
//        result.x = (int) (beginPt.getX()+u*dx);
//        result.y = (int) (beginPt.getY()+u*dy);
//        
//        return result;
//    }

	private final static double TWO_PI = 2 * PI;
	private final static double THREE_PI_OVER_TWO = 3 * PI / 2;
	private final static double PI_OVER_TWO = PI / 2;
	private final static double PI_OVER_FOUR = PI / 4;
	private final static double PI_OVER_EIGHT = PI / 8;
	public static final double NEAR_DELTA = .00001;

	private final static double NORTH = 0 * PI_OVER_FOUR;
	private final static double NORTH_EAST = 1 * PI_OVER_FOUR;
	private final static double EAST = 2 * PI_OVER_FOUR;
	private final static double SOUTH_EAST = 3 * PI_OVER_FOUR;
	private final static double SOUTH = 4 * PI_OVER_FOUR;
	private final static double SOUTH_WEST = 5 * PI_OVER_FOUR;
	private final static double WEST = 6 * PI_OVER_FOUR;
	private final static double NORTH_WEST = 7 * PI_OVER_FOUR;

	// Hide the default constructor as this class only provides static method
	private Utils() {
	}

	/**
	 * Method that returns the angle to a coordinate (tx,ty) from our robot.
	 * 
	 * @param x is the x coordinate.
	 * @param y is the y coordinate.
	 * @return the angle to the coordinate (tx,ty).
	 */
	private static double angleTo(int x, int y, double tx, double ty) {

		return Math.atan2(x - tx, y - ty);
	}

	/**
	 * Method that returns the angle to a coordinate (tx,ty) from our robot.
	 * 
	 * @param x is the x coordinate.
	 * @param y is the y coordinate.
	 * @return the angle to the coordinate (tx,ty).
	 */
	public static int angleToDegress(int x, int y, int tx, int ty) {
		return (int) Math.toDegrees(angleTo(x, y, tx, ty));
	}

	/**
	 * Normalizes an angle to an absolute angle. The normalized angle will be in the
	 * range from 0 to 2*PI, where 2*PI itself is not included.
	 *
	 * @param angle the angle to normalize
	 * @return the normalized angle that will be in the range of [0,2*PI[
	 */
	public static double normalAbsoluteAngle(double angle) {
		return (angle %= TWO_PI) >= 0 ? angle : (angle + TWO_PI);
	}

	/**
	 * Normalizes an angle to an absolute angle. The normalized angle will be in the
	 * range from 0 to 360, where 360 itself is not included.
	 *
	 * @param angle the angle to normalize
	 * @return the normalized angle that will be in the range of [0,360[
	 */
	public static double normalAbsoluteAngleDegrees(double angle) {
		return (angle %= 360) >= 0 ? angle : (angle + 360);
	}

	/**
	 * Normalizes an angle to a relative angle. The normalized angle will be in the
	 * range from -PI to PI, where PI itself is not included.
	 *
	 * @param angle the angle to normalize
	 * @return the normalized angle that will be in the range of [-PI,PI[
	 */
	public static double normalRelativeAngle(double angle) {
		return (angle %= TWO_PI) >= 0 ? (angle < PI) ? angle : angle - TWO_PI : (angle >= -PI) ? angle : angle + TWO_PI;
	}

	/**
	 * Normalizes an angle to a relative angle. The normalized angle will be in the
	 * range from -180 to 180, where 180 itself is not included.
	 *
	 * @param angle the angle to normalize
	 * @return the normalized angle that will be in the range of [-180,180[
	 */
	public static double normalRelativeAngleDegrees(double angle) {
		return (angle %= 360) >= 0 ? (angle < 180) ? angle : angle - 360 : (angle >= -180) ? angle : angle + 360;
	}

	/**
	 * Normalizes an angle to be near an absolute angle. The normalized angle will
	 * be in the range from 0 to 360, where 360 itself is not included. If the
	 * normalized angle is near to 0, 90, 180, 270 or 360, that angle will be
	 * returned. The {@link #isNear(double, double) isNear} method is used for
	 * defining when the angle is near one of angles listed above.
	 *
	 * @param angle the angle to normalize
	 * @return the normalized angle that will be in the range of [0,360[
	 * @see #normalAbsoluteAngle(double)
	 * @see #isNear(double, double)
	 */
	public static double normalNearAbsoluteAngleDegrees(double angle) {
		angle = (angle %= 360) >= 0 ? angle : (angle + 360);

		if (isNear(angle, 180)) {
			return 180;
		} else if (angle < 180) {
			if (isNear(angle, 0)) {
				return 0;
			} else if (isNear(angle, 90)) {
				return 90;
			}
		} else {
			if (isNear(angle, 270)) {
				return 270;
			} else if (isNear(angle, 360)) {
				return 0;
			}
		}
		return angle;
	}

	/**
	 * Normalizes an angle to be near an absolute angle. The normalized angle will
	 * be in the range from 0 to 2*PI, where 2*PI itself is not included. If the
	 * normalized angle is near to 0, PI/2, PI, 3*PI/2 or 2*PI, that angle will be
	 * returned. The {@link #isNear(double, double) isNear} method is used for
	 * defining when the angle is near one of angles listed above.
	 *
	 * @param angle the angle to normalize
	 * @return the normalized angle that will be in the range of [0,2*PI[
	 * @see #normalAbsoluteAngle(double)
	 * @see #isNear(double, double)
	 */
	public static double normalNearAbsoluteAngle(double angle) {
		angle = (angle %= TWO_PI) >= 0 ? angle : (angle + TWO_PI);

		if (isNear(angle, PI)) {
			return PI;
		} else if (angle < PI) {
			if (isNear(angle, 0)) {
				return 0;
			} else if (isNear(angle, PI_OVER_TWO)) {
				return PI_OVER_TWO;
			}
		} else {
			if (isNear(angle, THREE_PI_OVER_TWO)) {
				return THREE_PI_OVER_TWO;
			} else if (isNear(angle, TWO_PI)) {
				return 0;
			}
		}
		return angle;
	}

	/**
	 * Tests if the two {@code double} values are near to each other. It is
	 * recommended to use this method instead of testing if the two doubles are
	 * equal using an this expression: {@code value1 == value2}. The reason being,
	 * that this expression might never become {@code true} due to the precision of
	 * double values. Whether or not the specified doubles are near to each other is
	 * defined by the following expression:
	 * {@code (Math.abs(value1 - value2) &lt; .00001)}
	 *
	 * @param value1 the first double value
	 * @param value2 the second double value
	 * @return {@code true} if the two doubles are near to each other; {@code false}
	 *         otherwise.
	 */
	public static boolean isNear(double value1, double value2) {
		return (Math.abs(value1 - value2) < NEAR_DELTA);
	}

	/**
	 * Throws AssertionError when the param value is null. It could be used to
	 * express validation of invariant.
	 * 
	 * @param message of the eventual error
	 * @param value   tested value
	 */
	public static void assertNotNull(String message, Object value) {
		if (value == null) {
			throw new AssertionError(message);
		}
	}

	/**
	 * Throws AssertionError when the params expected and actual do not equal each
	 * other. It could be used to express validation of invariant.
	 * 
	 * @param message  of the eventual error
	 * @param expected expected value
	 * @param actual   tested value
	 */
	public static void assertEquals(String message, Object expected, Object actual) {
		if (expected == null && actual == null) {
			return;
		}
		if (expected == null || actual == null) {
			throw new AssertionError(message);
		}
		if (!expected.equals(actual)) {
			throw new AssertionError(message);
		}
	}

	/**
	 * Throws AssertionError when the assertion is false. It could be used to
	 * express validation of invariant.
	 * 
	 * @param message   of the eventual error
	 * @param assertion expected to be true
	 */
	public static void assertTrue(String message, boolean assertion) {
		if (!assertion) {
			throw new AssertionError(message);
		}
	}

	/**
	 * Throws AssertionError when the params expected and actual do not within
	 * .00001 difference. It could be used to express validation of invariant.
	 * 
	 * @param message  of the eventual error
	 * @param expected expected value
	 * @param actual   tested value
	 */
	public static void assertNear(String message, double expected, double actual) {
		if (!isNear(expected, actual)) {
			throw new AssertionError(message + " expected:" + expected + " actual:" + actual);
		}
	}

	/**
	 * Returns approximate cardinal direction for absolute angle in radians, like
	 * N,NE,E,SE,S,SW,W,NW
	 * 
	 * @param angle absolute angle in radians
	 * @return N,NE,E,SE,S,SW,W,NW
	 */
	public static String angleToApproximateDirection(double angle) {
		double absoluteAngle = normalAbsoluteAngle(angle);
		if (absoluteAngle < NORTH + PI_OVER_EIGHT) {
			return "N";
		} else if (absoluteAngle < NORTH_EAST + PI_OVER_EIGHT) {
			return "NE";
		} else if (absoluteAngle < EAST + PI_OVER_EIGHT) {
			return "E";
		} else if (absoluteAngle < SOUTH_EAST + PI_OVER_EIGHT) {
			return "SE";
		} else if (absoluteAngle < SOUTH + PI_OVER_EIGHT) {
			return "S";
		} else if (absoluteAngle < SOUTH_WEST + PI_OVER_EIGHT) {
			return "SW";
		} else if (absoluteAngle < WEST + PI_OVER_EIGHT) {
			return "W";
		} else if (absoluteAngle < NORTH_WEST + PI_OVER_EIGHT) {
			return "NW";
		} else {
			return "N";
		}
	}

	public static int distanceTo(int x, int y, int tx, int ty) {
		return (int) Math.hypot(x - tx, y - ty);
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

//	public static void getNextBulletByTick2(int nowX, int nowY, int r, int tick) {
//		float rr = Utils.a2r(r);
//		// startx + kProjectileSpeed * cosf(rr) * elapsed;
//		BigDecimal dx = new BigDecimal(String.valueOf(Math.cos(rr)));
//		BigDecimal x = dx.multiply(BigDecimal.valueOf(tick)).multiply(BigDecimal.valueOf(AppConfig.BULLET_SPEED))
//				.add(BigDecimal.valueOf(nowX)).setScale(1, BigDecimal.ROUND_DOWN);
//		// double x = (nowX + AppConfig.BULLET_SPEED * Math.cos(rr) * tick);
//
//		// starty + kProjectileSpeed * sinf(rr) * elapsed;
//		BigDecimal dy = new BigDecimal(String.valueOf(Math.sin(rr)));
//		BigDecimal y= dy.multiply(BigDecimal.valueOf(tick)).multiply(BigDecimal.valueOf(AppConfig.BULLET_SPEED))
//				.add(BigDecimal.valueOf(nowY)).setScale(0, BigDecimal.ROUND_HALF_DOWN);
//		// double y = (nowY + AppConfig.BULLET_SPEED * Math.sin(rr) * tick);
//		System.out
//				.println(String.format("[%s,%s]",String.valueOf(x.doubleValue()),String.valueOf(y.doubleValue())));
//		// return new Position(x, y);
//	}

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
		// log.debug("willhit,distance=" + distance);
		// 垂足小于半径，会打到
		// todo 这里有Bug,还需要判断方向
		return distance < targetWidth;

	}

	// 求夹角,返回[0,180]
	public static int bearing(int angle1, int angle2) {
		int a1 = (int) normalAbsoluteAngleDegrees(angle1);
		int a2 = (int) normalAbsoluteAngleDegrees(angle2);
		int ret = Math.abs(a1 - a2);
		ret = (ret + 180) % 180;
		return ret;
	}

	//
	public static Position getNextPostion(int x, int y, int r, int tick) {
		float angle = a2r(r);
		int dy = (int) (AppConfig.TANK_SPEED * tick * Math.sin(angle));
		int dx = (int) (AppConfig.TANK_SPEED * tick * Math.cos(angle));

		int nx = x + dx;
		int ny = y + dy;
		return new Position(nx, ny);
	}

	public static boolean isNear(Position p1, Position p2) {
		return (Math.abs(p1.x - p2.x) <= 1) && (Math.abs(p1.y - p2.y) <= 1);
	}

	public static boolean isNear(int x1, int y1, int x2, int y2) {
		return Math.abs(x1 - x2) <= 1 && Math.abs(y1 - y2) <= 1;
	}
}
