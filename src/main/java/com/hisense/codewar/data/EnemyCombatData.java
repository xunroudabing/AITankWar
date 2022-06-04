package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.hisense.codewar.config.AppConfig;

public class EnemyCombatData {
	public int tankid;
	public int teamid;
	public boolean isAlive;
	public MovementTrack trackData;
	public List<MovementTrack> historyTracks;

	public EnemyCombatData(int tankID, int teamID) {
		tankid = tankID;
		teamid = teamID;
		historyTracks = new ArrayList<EnemyCombatData.MovementTrack>(2000);
	}

	public static class MovementTrack implements Cloneable {
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
		/**
		 * 运动方向
		 */
		public int absAngle;
		public int speed;
		public int x;
		public int y;
		//与我的距离
		public int dist;
		// 我的子弹
		public int bulletAngle;
		// 时间戳
		public int tick;

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return String.format("velSeg[%f]adSeg[%f]speed[%d]pos[%d,%d]absAngle[%d]dist[%d]tick[%d]", velSeg, adSeg, speed, x,
					y, absAngle,dist,tick);
		}

		@Override
		protected MovementTrack clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			MovementTrack ret = (MovementTrack) super.clone();
			ret.velSeg = this.velSeg;
			ret.adSeg = this.adSeg;
			ret.speed = this.speed;
			ret.x = this.x;
			ret.y = this.y;
			ret.dist = this.dist;
			ret.bulletAngle = this.bulletAngle;
			ret.tick = this.tick;
			return ret;
		}
	}

	public void addToHistroy(MovementTrack track) {
		if (historyTracks.size() < AppConfig.WAVE_DB_MAXROWS) {
			historyTracks.add(track);
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
		if (obj instanceof EnemyCombatData) {
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
