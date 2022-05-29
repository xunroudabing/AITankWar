package com.hisense.codewar.wave;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;

public class Wave {
	public static void main(String[] args) {
		int x1 = 200;
		int y1 = 200;

		int x2 = 300;
		int y2 = 200;

		int startX = 0;
		int startY = 0;
		int absBearing = Utils.angleTo(startX, startY, x1, y1);
		int angleCurrent = Utils.angleTo(startX, startY, x2, y2);
		int aimAngle = Utils.normalRelativeAngleDegrees(Utils.formatAngle(angleCurrent - absBearing));

		System.out.println("absBearing=" + absBearing + ",angleCurrent=" + angleCurrent + ",aim=" + aimAngle);
	}

	private Position myStartPos = null;

	private int absBearing = 0;

	private int startTick;

	private int dist;
	/**
	 * 速度垂直分量
	 */
	private double adSeg;
	/**
	 * 速度水平分量
	 */
	private double velSeg;
	/**
	 * 匹配的射击角度
	 */
	private int angle;
	private int enemyTankId;
	private int speed;

	public Wave() {

	}

	public HitTestResult hitTest(int tick, TankGameInfo tank) {
		HitTestResult result = new HitTestResult();
		int tickPassed = tick - startTick;
		// 波目前的传播距离
		int waveDistance = AppConfig.BULLET_SPEED * tickPassed;
		int distance = Utils.distanceTo(myStartPos.x, myStartPos.y, tank.x, tank.y);
		// 波击中目标
		if (waveDistance >= distance + AppConfig.TARGET_RADIUS * 2) {

			int angleCurrent = Utils.angleTo(myStartPos.x, myStartPos.y, tank.x, tank.y);
			int aimAngle = Utils.normalRelativeAngleDegrees(Utils.formatAngle(angleCurrent - absBearing));

			result.ret = true;
			result.suggestAimAngle = aimAngle;
			return result;
		}
		result.ret = false;
		return result;
	}

	public Position getMyStartPos() {
		return myStartPos;
	}

	public void setMyStartPos(Position myStartPos) {
		this.myStartPos = myStartPos;
	}

	public double getAbsBearing() {
		return absBearing;
	}

	public void setAbsBearing(int absBearing) {
		this.absBearing = absBearing;
	}

	public int getStartTick() {
		return startTick;
	}

	public void setStartTick(int startTick) {
		this.startTick = startTick;
	}

	public double getDist() {
		return dist;
	}

	public void setDist(int dist) {
		this.dist = dist;
	}

	public double getAdSeg() {
		return adSeg;
	}

	public void setAdSeg(double adSeg) {
		this.adSeg = adSeg;
	}

	public double getVelSeg() {
		return velSeg;
	}

	public void setVelSeg(double velSeg) {
		this.velSeg = velSeg;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public int getEnemyTankId() {
		return enemyTankId;
	}

	public void setEnemyTankId(int enemyTankId) {
		this.enemyTankId = enemyTankId;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

}
