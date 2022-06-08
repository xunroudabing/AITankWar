package com.hisense.codewar.utils;

import static java.lang.Math.PI;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankMapBlock;

public class Utils {
	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	public static void main(String[] args) {
//		int x1 = 0;
//		int y1 = 0;
//
//		int x2 = 10;
//		int y2 = 0;
//
//		int x3 = 5;
//		int y3 = 0;

//		Position p4 = Utils.getFoot(new Position(x1, y1), new Position(x2, y2), new Position(x3, y3));
//		System.out.println(p4);

//		Position p1 = new Position(5, 5);
//		Position p2 = new Position(0, 5);
//
//		Position p3 = new Position(10, 0);
//		Position p4 = new Position(10, 10);
//
//		Position position = Utils.crossPoint(p1, p2, p3, p4);
//		System.out.println(position);
//
//		int bearing = Utils.bearing(180, 0);
//		System.out.println(bearing);

		// [529,394]xforce[-36]yforce[-53]result[565,447]

		int x1 = 895;
		int y1 = 355;

		int x2 = 565;
		int y2 = 447;

		int angle1 = Utils.angleTo(x1, y1, x2, y2);

		int angel2 = Utils.angleTo2(x1, y1, x2, y2);

		System.out.println(angle1);
		System.out.println(angel2);

		for (int i = 0; i < 10; i++) {
			Position position = Utils.getNextPositionByDistance(x1, y1, 0, 9 * i);
			System.out.println(position);
		}
		
		int x3 = 100;
		int y3 = 100;
		
		int x4 = 110;
		int y4 = 110;
		
		int range = Utils.getFireRange(x3, y3, x4, y4);
		System.out.println("range=" + range);

	}

	/**
	 * 计算所需时间，向上取整
	 * 
	 * @param distance
	 * @param speed
	 * @return
	 */
	public static int getTicks(int distance, int speed) {
		BigDecimal b = BigDecimal.valueOf(distance);
		// distance / AppConfig.TANK_SPEED; CEILING，向上取整
		int tick = b.divide(BigDecimal.valueOf(speed), 0, BigDecimal.ROUND_CEILING).intValue();
		return tick;
	}

	/**
	 * 开火方向
	 * 
	 * @param nowx
	 * @param nowy
	 * @param tx   目标
	 * @param ty   目标
	 * @return
	 */
	public static int angleTo(int nowx, int nowy, int tx, int ty) {
		int ret = 0;
		if (tx == nowx) {
			if (ty > nowy) {
				ret = 90;
			} else {
				ret = 270;
			}
		}

		else if (ty == nowy) {
			if (tx > nowx) {
				ret = 0;
			} else {
				ret = 180;
			}
		} else {
			ret = (int) r2a(Math.atan2(nowy - ty, nowx - tx));

		}
		// bearing角，[-180~180]
		if ((tx < nowx && ty < nowy) || (tx > nowx && ty > nowy)) {
			ret += 180;
		} else if ((tx > nowx && ty < nowy) || (tx < nowx && ty > nowy)) {
			ret -= 180;
		}
		return ret;
	}

	public static int angleTo2(int nowx, int nowy, int tx, int ty) {
		int dest_deg = 0;
		if (tx == nowx) {
			if (ty > nowy) {
				dest_deg = 90;
			} else {
				dest_deg = 270;
			}
		} else {
			dest_deg = r2a(Math.atan((float) (ty - nowy) / (float) (tx) - nowx));
		}
		if (tx < nowx && ty < nowy || (tx < nowx && ty > nowy)) {
			dest_deg += 180;
		}
		return dest_deg;
	}

	public static int formatAngle(int angle) {
		int a = angle;
		if (a < 0) {
			a += 360;
		}
		return (a + 360) % 360;
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

		return (int) Math.round(radian * 180.0f / 3.14159265358979f);
	}

	/**
	 * 获取目标角度 bug
	 * 
	 * @param tx
	 * @param ty
	 * @param nowx
	 * @param nowy
	 * @return
	 */
	@Deprecated
	public static int angleTo4(int nowx, int nowy, int tx, int ty) {
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
	public static int getAngle3(int ax, int ay, int bx, int by, int cx, int cy) {

		int c = distanceTo(ax, ay, bx, by);
		int b = distanceTo(ax, ay, cx, cy);

		int a = distanceTo(bx, by, cx, cy);

		if (b == 0 || c == 0) {
			return -1;
		}
		double d = Math.acos((b * b + c * c - a * a) / (2.0d * b * c));
		log.debug("d=" + Math.toDegrees(d));
		int angle = (int) Math.toDegrees(d);
		return angle;
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
	 * Normalizes an angle to a relative angle. The normalized angle will be in the
	 * range from -180 to 180, where 180 itself is not included.
	 *
	 * @param angle the angle to normalize
	 * @return the normalized angle that will be in the range of [-180,180[
	 */
	public static int normalRelativeAngleDegrees(int angle) {
		return (angle %= 360) >= 0 ? (angle < 180) ? angle : angle - 360 : (angle >= -180) ? angle : angle + 360;
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

	public static int distanceTo(int x, int y, int tx, int ty) {
		return doubleToInt(Math.hypot(x - tx, y - ty));
	}

	/**
	 * double向上取整，1.1返回 2
	 * 
	 * @param d
	 * @return
	 */
	public static int doubleToInt(double d) {
		BigDecimal bigDecimal = BigDecimal.valueOf(d);
		BigDecimal result = bigDecimal.setScale(0, BigDecimal.ROUND_CEILING);
		return result.intValue();

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
	@Deprecated
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

		int rr = angleTo(bulletX, bulletY, foot.x, foot.y);
		if (rr * r < 0) {
			log.error("rr * r < 0");
			return false;
		}
		return distance < targetWidth;

	}

	// 求夹角,返回[0,180]
	public static int bearing(int angle1, int angle2) {
		// int a1 = (int) normalAbsoluteAngleDegrees(angle1);
		// int a2 = (int) normalAbsoluteAngleDegrees(angle2);
		int ret = Math.abs(angle1 - angle2);
		ret = (ret + 180) % 180;
		return ret;
	}

	// 坦克移动位置
	public static Position getNextTankPostion(int x, int y, int r, int tick) {
		float angle = a2r(r);
		int dy = doubleToInt((AppConfig.TANK_SPEED * tick * Math.sin(angle)));
		int dx = doubleToInt((AppConfig.TANK_SPEED * tick * Math.cos(angle)));

		int nx = x + dx;
		int ny = y + dy;
		return new Position(nx, ny);
	}

	// 返回x y r方向上 distance的点
	public static Position getNextPositionByDistance(int x, int y, int r, int distance) {
		float angle = a2r(r);
		double dy = y + (distance * Math.sin(angle));
		double dx = x + (distance * Math.cos(angle));

		int nx = doubleToInt(dx);
		int ny = doubleToInt(dy);
		return new Position(nx, ny);
	}

//	public static boolean isOutRange(int x, int y) {
//		if (x > AppConfig.MAP_WITH - AppConfig.TANK_SIZE || x < AppConfig.TANK_SIZE) {
//			return true;
//		} else if (y > AppConfig.MAP_HEIGHT - AppConfig.TANK_SIZE || y < AppConfig.TANK_SIZE) {
//			return true;
//		}
//		return false;
//	}
	
	public static boolean isNear(Position p1, Position p2) {
		return (Math.abs(p1.x - p2.x) <= 2) && (Math.abs(p1.y - p2.y) <= 2);
	}

	public static boolean isNear(int x1, int y1, int x2, int y2) {
		return Math.abs(x1 - x2) <= 2 && Math.abs(y1 - y2) <= 2;
	}

	public static int getFireRange(int nowx, int nowy, int tx, int ty) {
		int distance = distanceTo(nowx, nowy, tx, ty);
		double angle = Math.atan2(AppConfig.TARGET_RADIUS, distance);
		return r2a(angle);
	}

	/**
	 * 判断两条线是否相交 a 线段1起点坐标 b 线段1终点坐标 c 线段2起点坐标 d 线段2终点坐标 intersection 相交点坐标 reutrn
	 * 是否相交: 0 : 两线平行 -1 : 不平行且未相交 1 : 两线相交
	 */

	public static Position crossPoint(Position a, Position b, Position c, Position d) {
		Position intersection = new Position(0, 0);

		if (Math.abs(b.y - a.y) + Math.abs(b.x - a.x) + Math.abs(d.y - c.y) + Math.abs(d.x - c.x) == 0) {
			if ((c.x - a.x) + (c.y - a.y) == 0) {
				// System.out.println("ABCD是同一个点！");
			} else {
				// System.out.println("AB是一个点，CD是一个点，且AC不同！");
			}
			// 0
			return null;
		}

		if (Math.abs(b.y - a.y) + Math.abs(b.x - a.x) == 0) {
			if ((a.x - d.x) * (c.y - d.y) - (a.y - d.y) * (c.x - d.x) == 0) {
				// System.out.println("A、B是一个点，且在CD线段上！");
			} else {
				// System.out.println("A、B是一个点，且不在CD线段上！");
			}
			// return 0;
			return null;
		}
		if (Math.abs(d.y - c.y) + Math.abs(d.x - c.x) == 0) {
			if ((d.x - b.x) * (a.y - b.y) - (d.y - b.y) * (a.x - b.x) == 0) {
				// System.out.println("C、D是一个点，且在AB线段上！");
			} else {
				// System.out.println("C、D是一个点，且不在AB线段上！");
			}
			// return 0;
			return null;
		}

		if ((b.y - a.y) * (c.x - d.x) - (b.x - a.x) * (c.y - d.y) == 0) {
			// System.out.println("线段平行，无交点！");
			// return 0;
			return null;
		}

		intersection.x = ((b.x - a.x) * (c.x - d.x) * (c.y - a.y) - c.x * (b.x - a.x) * (c.y - d.y)
				+ a.x * (b.y - a.y) * (c.x - d.x)) / ((b.y - a.y) * (c.x - d.x) - (b.x - a.x) * (c.y - d.y));
		intersection.y = ((b.y - a.y) * (c.y - d.y) * (c.x - a.x) - c.y * (b.y - a.y) * (c.x - d.x)
				+ a.y * (b.x - a.x) * (c.y - d.y)) / ((b.x - a.x) * (c.y - d.y) - (b.y - a.y) * (c.x - d.x));

		if ((intersection.x - a.x) * (intersection.x - b.x) <= 0 && (intersection.x - c.x) * (intersection.x - d.x) <= 0
				&& (intersection.y - a.y) * (intersection.y - b.y) <= 0
				&& (intersection.y - c.y) * (intersection.y - d.y) <= 0) {

			// System.out.println("线段相交于点(" + intersection.x + "," + intersection.y + ")！");
			// return 1; // '相交
			return intersection;
		} else {
			// System.out.println("线段相交于虚交点(" + intersection.x + "," + intersection.y +
			// ")！");
			// return -1; // '相交但不在线段上
			return intersection;
		}

	}

	/**
	 * 对于给定的一个坦克中心坐标，判断是否在障碍物中
	 * 
	 * @param x
	 * @param y
	 * @param mWidth
	 * @param blocks
	 * @param blockWidth
	 * @return
	 */
	public static boolean inBlocks(int x, int y, int mWidth, List<TankMapBlock> blocks, int blockWidth) {
		if (blocks == null) {
			return false;
		}
		for (TankMapBlock block : blocks) {
			boolean ret = inBlock(x, y, mWidth, block.x, block.y, blockWidth);
			if (ret) {
				return true;
			}
		}
		return false;
	}

	public static boolean inBlock(int x, int y, int width, int blockX, int blockY, int blockWidth) {
		int totalR = width + blockWidth;
		int distance = Utils.distanceTo(x, y, blockX, blockY);

		return distance < totalR;
	}

	/**
	 * 是否穿过Block
	 * 
	 * @param x
	 * @param y
	 * @param r
	 * @param distance
	 * @param blocks
	 * @param blockWidth
	 * @return
	 */
	public static boolean isCrossBlockByDistance(int x, int y, int r, int distance, List<TankMapBlock> blocks,
			int blockWidth) {
		Position endPostion = Utils.getNextPositionByDistance(x, y, r, distance);
		return isCrossBlock(x, y, endPostion.x, endPostion.y, blocks, blockWidth);
	}

	/**
	 * 靠近Block时，判断下一个移动位置是否和block相交，如果相交则代表无法移动
	 * 
	 * @param x
	 * @param y
	 * @param r
	 * @param distance
	 * @param blocks
	 * @param blockWidth
	 * @return
	 */
	public static boolean isNextPointInBlocks(int x, int y, int r, int distance, List<TankMapBlock> blocks,
			int blockWidth) {
		Position endPostion = Utils.getNextPositionByDistance(x, y, r, distance);
		return inBlocks(endPostion.x, endPostion.y, blockWidth, blocks, blockWidth);
	}

	/**
	 * 两点的直线是否穿过Block
	 * 
	 * @param x
	 * @param y
	 * @param tx
	 * @param ty
	 * @param blocks
	 * @param blockWidth
	 * @return
	 */
	public static boolean isCrossBlock(int x, int y, int tx, int ty, List<TankMapBlock> blocks, int blockWidth) {
		// 以我到目标为直线 ，Block向该直线做垂线，如果垂线距离小于半径则说明与block相交
		Position p1 = new Position(x, y);
		Position p2 = new Position(tx, ty);

		int distance = Utils.distanceTo(x, y, tx, ty);
		for (TankMapBlock block : blocks) {
			Position p3 = new Position(block.x, block.y);
			// 垂足
			Position p4 = Utils.getFoot(p1, p2, p3);
			// 垂足到圆心距离
			int c = Utils.distanceTo(p3.x, p3.y, p4.x, p4.y);
			// 会打中block
			if (c <= blockWidth) {
				// 我到垂足的距离
				int d = Utils.distanceTo(x, y, p4.x, p4.y);
				if (d < distance) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
     * 判断射击路线是否被障碍物阻挡
     * @param xs 坦克中心点的横坐标
     * @param ys 坦克中心点的纵坐标
     * @param xe 目标坦克中心点的横坐标
     * @param ye 目标坦克中心点的纵坐标
     * @param r 射击角度
     * @param blocks 障碍物列表
     * @return Boolean
     */
    public static boolean isStopFireByBlock(Double xs, Double ys, Double xe, Double ye, Double r, List<TankMapBlock> blocks) {
//        for(TankMapBlock i : blocks){
//            System.out.println("block X: " + i.getX() + " Y: " + i.getY());
//        }
//        System.out.println("isStopFireByBlock: xs=" + xs + ", ys=" + ys + ", xe=" + xe + ", ye=" + ye + ", r=" + r );
        List<Double> rectangle = getRectangleByLine(xs, ys, xe, ye, r, 30.0);
        for (TankMapBlock tankMapBlock : blocks) {
            if (judgeIsPointInRectangle((double)tankMapBlock.getX(), (double)tankMapBlock.getY(), rectangle.get(0),
                    rectangle.get(1), rectangle.get(2), rectangle.get(3), rectangle.get(4), rectangle.get(5),
                    rectangle.get(6), rectangle.get(7))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 根据线段、角度以及宽度获取长方形
     * @param xs 起点的横坐标
     * @param ys 起点的纵坐标
     * @param xe 终点的横坐标
     * @param ys 终点的纵坐标
     * @param r 字段方向
     * @param edge 边长的一半
     * @return Double
     */
    private static List<Double> getRectangleByLine(Double xs, Double ys, Double xe, Double ye, Double r, Double edge) {
        Double x1 = Math.cos(r/180.0 * Math.PI + Math.PI/2.0) * edge + xe;
        Double y1 = Math.sin(r/180.0 * Math.PI + Math.PI/2.0) * edge + ye;
        Double x2 = Math.cos(r/180.0 * Math.PI + Math.PI/2.0) * edge + xs;
        Double y2 = Math.sin(r/180.0 * Math.PI + Math.PI/2.0) * edge + ys;
        Double x3 = Math.cos(r/180.0 * Math.PI + Math.PI * 3 /2.0) * edge + xs;
        Double y3 = Math.sin(r/180.0 * Math.PI + Math.PI * 3 /2.0) * edge + ys;
        Double x4 = Math.cos(r/180.0 * Math.PI + Math.PI * 3 /2.0) * edge + xe;
        Double y4 = Math.sin(r/180.0 * Math.PI + Math.PI * 3 /2.0) * edge + ye;

        List<Double> rectangle = new ArrayList<>();
        rectangle.add(x1);
        rectangle.add(y1);
        rectangle.add(x2);
        rectangle.add(y2);
        rectangle.add(x3);
        rectangle.add(y3);
        rectangle.add(x4);
        rectangle.add(y4);
        return rectangle;
    }
    
    /**
     * 判断一个点是否造长方形内
     * @param x 点的横坐标
     * @param y 点的纵坐标
     * @param x1 长方形左上方顶点横坐标
     * @param y1 长方形左上方顶点纵坐标
     * @param x2 长方形左下方顶点横坐标
     * @param y2 长方形左下方顶点纵坐标
     * @param x3 长方形右下方顶点横坐标
     * @param y3 长方形右下方顶点纵坐标
     * @param x4 长方形右上方顶点横坐标
     * @param y4 长方形右上方顶点纵坐标
     * @return Double
     */
    private static Boolean judgeIsPointInRectangle(Double x, Double y, Double x1, Double y1, Double x2, Double y2, Double x3, Double y3, Double x4, Double y4) {
        // 计算点到各边的距离之和
        Double length1 = calDistanceOfPointToLine(x, y, x1, y1, x2, y2);
        Double length2 = calDistanceOfPointToLine(x, y, x2, y2, x3, y3);
        Double length3 = calDistanceOfPointToLine(x, y, x3, y3, x4, y4);
        Double length4 = calDistanceOfPointToLine(x, y, x4, y4, x1, y1);
        Double lengthSum = length1 + length2 + length3 + length4;

        // 计算长方向周长
        Double lengthAB = calLineSpace(x1, y1, x2, y2);
        Double lengthBC = calLineSpace(x2, y2, x3, y3);
        Double lengthCD = calLineSpace(x3, y3, x4, y4);
        Double lengthDA = calLineSpace(x4, y4, x1, y1);
        Double perimeter = lengthAB + lengthBC + lengthCD + lengthDA;
        Double halfPerimeter = perimeter / 2.0;

        // 当点到各边的距离等于长方形周长的一半的时候，点在长方向内
        return Math.abs(halfPerimeter - lengthSum) < 0.00001;

    }
    
    /**
     * 计算点到线段的距离
     * @param x 点的横坐标
     * @param y 点的纵坐标
     * @param x1 线段起点的横坐标
     * @param y1 线段起点的纵坐标
     * @param x2 线段终点的横坐标
     * @param y2 线段终点的纵坐标
     * @return Double
     */
    public static Double calDistanceOfPointToLine(Double x, Double y, Double x1, Double y1, Double x2, Double y2) {
        // 1、计算各边长以及周长
        Double lengthAB = calLineSpace(x, y, x1, y1);
        Double lengthAC = calLineSpace(x, y, x2, y2);
        Double lengthBC = calLineSpace(x1, y1, x2, y2);
        Double perimeter = lengthAB + lengthAC + lengthBC;
        Double halfPerimeter = perimeter / 2.0;

        // 2、计算坦克位置、子弹初始位置、子弹末端位置三点构成的三角形面积
        Double area = Math.sqrt(halfPerimeter * (halfPerimeter - lengthAB) * (halfPerimeter - lengthAC) * (halfPerimeter - lengthBC));

        // 3、计算点到直线的距离
        return 2 * area / lengthBC;
    }
    
    /**
     * 计算两点间的距离
     * @param x1 起点的横坐标
     * @param y1 起点的纵坐标
     * @param x2 终点的横坐标
     * @param y2 终点的纵坐标
     * @return Double
     */
    public static Double calLineSpace(Double x1, Double y1, Double x2, Double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
}
