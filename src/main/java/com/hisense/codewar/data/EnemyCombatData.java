package com.hisense.codewar.data;

import java.util.Objects;

public class EnemyCombatData {
	public int tankid;
	public int teamid;
	public boolean isAlive;
	public MovementTrack trackData;

	public EnemyCombatData(int tankID, int teamID) {
		tankid = tankID;
		teamid = teamID;
	}

	public static class MovementTrack {
		// velSeg = e.getVelocity() * Math. cos (e.getHeadingRadians() - absBearing );
		// adSeg = e.getVelocity() * Math. sin (e.getHeadingRadians() - absBearing );
		/**
		 * 垂直分量
		 */
		public double velSeg;
		/**
		 * 水平分量
		 */
		public double adSeg;
		public int absAngle;
		public int speed;
		public int x;
		public int y;
		// 我的子弹
		public int bulletAngle;
		// 时间戳
		public int tick;
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return String.format("velSeg[%f]adSeg[%f]speed[%d]pos[%d,%d]absAngle[%d]tick[%d]", velSeg,adSeg,speed,x,y,absAngle,tick);
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("[EnemyCombatData]tankid[%d]%s", tankid, trackData.toString());
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof EnemyCombatData) {
			EnemyCombatData o = (EnemyCombatData) obj;
			return tankid == o.tankid;
		}
		return false;
	}
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return Objects.hash(tankid);
	}
}
