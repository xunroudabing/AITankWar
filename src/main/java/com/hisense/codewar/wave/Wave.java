package com.hisense.codewar.wave;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;

public class Wave {
	private Position myStartPos = null;

	private double absBearing = 0;

	private int startTick;

	private double dist;
	/**
	 * 速度垂直分量
	 */
	private double adSeg;
	/**
	 * 速度水平分量
	 */
	private double velSeg;

	private double angle;
	
	public Wave() {
		
	}
	
	public boolean hitTest(int tick,TankGameInfo tank) {
		int tickPassed = tick - startTick;
		//波目前的传播距离
		int waveDistance = AppConfig.BULLET_SPEED * tickPassed;
		int distance = Utils.distanceTo(myStartPos.x, myStartPos.y, tank.x, tank.y);
		if(waveDistance >= distance) {
			
		}
		return false;
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

	public void setAbsBearing(double absBearing) {
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

	public void setDist(double dist) {
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

	public void setAngle(double angle) {
		this.angle = angle;
	}

}
