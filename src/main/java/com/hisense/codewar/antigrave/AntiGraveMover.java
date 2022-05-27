package com.hisense.codewar.antigrave;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;

public class AntiGraveMover {
	private int mTick = 0;
	private List<GravePoint> mGravePoints;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(AntiGraveMover.class);

	public AntiGraveMover(CombatRealTimeDatabase database) {
		mDatabase = database;
		mGravePoints = new ArrayList<GravePoint>();
	}

	public void antiGraveMove() {

		int force = 0;
		int xforce = 0;
		int yforce = 0;
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		for (GravePoint p : mGravePoints) {
			force = (int) (p.getPower() / Math.pow(Utils.distanceTo(nowX, nowY, p.getX(), p.getY()), 2));
			// Find the bearing from the point to us
			// int angle = normaliseBearing(Math.PI / 2 - Math.atan2(getY() - p.y, getX() -
			// p.x));
			int angle = Utils.angleTo(p.getX(), p.getY(), nowX, nowY);
			double angleToR = Utils.a2r(angle);
			// Add the components of this force to the total force in their respective
			// directions
			xforce += Math.sin(angleToR) * force;
			yforce += Math.cos(angleToR) * force;
		}
		// blocks
		xforce += 5000 / Math.pow(Utils.distanceTo(nowX, nowY, mDatabase.getBattleFieldWidth(), nowY), 3);
		xforce -= 5000 / Math.pow(Utils.distanceTo(nowX, nowY, 0, nowY), 3);
		yforce += 5000 / Math.pow(Utils.distanceTo(nowX, nowY, nowX, mDatabase.getBattleFieldHeight()), 3);
		yforce -= 5000 / Math.pow(Utils.distanceTo(nowX, nowY, nowX, 0), 3);

		// Move in the direction of our resolved force.
		goTo(nowX - xforce, nowY - yforce);
	}

	public void scan(int tick) {

		List<GravePoint> enemyPoints = createGravePointsByEnemys();
		mGravePoints.addAll(enemyPoints);
	}

	/** Move towards an x and y coordinate **/
	public void goTo(int x, int y) {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		double dist = 20;
		int angle = Utils.angleTo(nowX, nowY, x, y);
		// double angle = Math.toDegrees(absbearing(getX(), getY(), x, y));

	}

	protected int getEnemyPower() {
		return -1000;
	}

	protected List<GravePoint> createGravePointsByEnemys() {
		List<GravePoint> points = new ArrayList<>();
		List<TankGameInfo> list = mDatabase.getEnemyTanks();
		for (TankGameInfo tank : list) {
			GravePoint gPoint = createGravePoint(tank);
			points.add(gPoint);
		}
		return points;
	}

	protected GravePoint createGravePoint(TankGameInfo tank) {
		GravePoint point = new GravePoint(tank.x, tank.y, getEnemyPower());
		return point;
	}
}
