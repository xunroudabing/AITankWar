package com.hisense.codewar.utils;

import static java.lang.Math.PI;

import java.awt.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	public static void main(String[] args) {
		int angle = 180;
		//839,818]r[355]distance[630]-->result[0]me[1465,746
//		int x1 = 839;
//		int y1 = 818;
//
//		int x2 = 1465;
//		int y2 = 746;
//
//		// double a = r2a(Math.atan((float) (y2 - y1) / (float) x2 - x1));
//
//		int a = Utils.getTargetRadius(x2, y2, x1, y1);
//
//		int g = Utils.getTargetRadius(x1, y1, x2, y2);
////
////		double c = Math.toDegrees(b);
////
//		int d = Utils.angleToDegress(x1, y1, x2, y2);
//		int e = Utils.getFireAngle(x1, y1, x2, y2);
//		int f = Utils.getFireAngle(x2, y2, x1, y1);
//		
//		System.out.println(a);
//		System.out.println(g);
//		System.out.println(e);
//		System.out.println(f);
//		
//		System.out.println(d);
//		
//		int d360 = Utils.formatAngle(-83);
//		System.out.println("360:"+d360);
		
		//827,819    839,818     1465,746   r 355
		
		int x1 = 827;
		int y1 = 819;
		
		int x2 = 839;
		int y2 = 818;
		
		int mx = 1465;
		int my = 746;
		
		int a = Utils.getTargetRadius(x2, y2, x1, y1);
		
		int b = Utils.getFireAngle(x1, y1, mx, my);
		
		System.out.println(a);
		
		System.out.println(b);
		
		System.out.println(Utils.formatAngle(a));
		
		System.out.println(Utils.formatAngle(b));
		
		Point d = Utils.getFoot( new Point(mx, my),new Point(x1, y1), new Point(x2, y2));
		
		System.out.println(d);
		
		Point d2 = Utils.getFoot2(new Point(x1, y1), new Point(x2, y2),new Point(mx, my));
		
		System.out.println(d2);
		
		int distance = Utils.getDistance(mx, my, d2.x, d2.y);
		
		System.out.println("distance=" + distance);
		
		

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

	public static int distanceTo(int x, int y, int tx, int ty) {
		return (int) Math.hypot(x - tx, y - ty);
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
		//return getFireAngle(nowx, nowy, tx, ty);
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
		log.debug("d=" +Math.toDegrees(d));
		int angle = (int) Math.toDegrees(d);
		return angle;
	}
	
	public static boolean inline(int x1,int y1,int x2,int y2,int tx,int ty) {
		boolean ret = (tx-x1)*(y1-y2) == (x1-x2)*(ty-y1);
		return ret;
		
	}
	
	public static Point getFoot2(Point p1,Point p2,Point p3){
        Point foot=new Point();
        
        float dx=p1.x-p2.x;
        float dy=p1.y-p2.y;
        
        float u=(p3.x-p1.x)*dx+(p3.y-p1.y)*dy;
        u/=dx*dx+dy*dy;
        
        foot.x=(int)(p1.x+u*dx);
        foot.y=(int)(p1.y+u*dy);
        
        return foot;
    }
	
	
	   //点到其他两点垂足计算
    public static Point getFoot(Point pt,Point beginPt,Point endPt){
        Point result = new Point();
        double dx = beginPt.getX()-endPt.getX();
        double dy = beginPt.getY()-endPt.getY();
 
        if(Math.abs(dx)<0.000001&&Math.abs(dy)<0.000001){
            result = pt;
        }
 
        double u = (pt.getX()-beginPt.getX())*(beginPt.getX()-endPt.getX())+
                (pt.getY()-beginPt.getY())*(beginPt.getY()-endPt.getY());
        log.debug("u=" + u);
        u = u /(dx*dx+dy*dy);
        result.x = (int) (beginPt.getX()+u*dx);
        result.y = (int) (beginPt.getY()+u*dy);
        
        return result;
    }

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

}
