package com.hisense.codewar.data;

public class EnemyCombatData {
	public int tankid;
	public int teamid;
	public boolean isAlive;
	public LimitedQueue<MovementTrack> trackData;

	public EnemyCombatData(int tankID, int teamID) {
		tankid = tankID;
		teamid = teamID;
		trackData = new LimitedQueue<>(100);
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
		//我的子弹
		public int bulletAngle;
		//时间戳
		public int tick;
	}
}
