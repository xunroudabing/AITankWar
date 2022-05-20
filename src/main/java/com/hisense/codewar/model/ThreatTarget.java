package com.hisense.codewar.model;

import java.util.Objects;

public class ThreatTarget implements Comparable<ThreatTarget> {
	public String bulletInfoId;
	public int tankId;
	public int tankX;
	public int tankY;
	public int tankHeading;

	public int bulletX;
	public int bulletY;
	public int bulletR;

	// 剩余时间
	public int ticketLeft;
	// 会命中
	public boolean willHit = true;
	// 最佳闪避方向
	public int dodgeBestR;
	// 最佳闪避移动距离
	public int dodgeBestDis;
	// 最佳闪避所需tick
	public int dodgeBestTick;
	// 最终闪避方向
	public int dodgeFinalR;
	public boolean handled = false;

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof ThreatTarget) {
			ThreatTarget o = (ThreatTarget) obj;
			if (bulletInfoId.equals(o.bulletInfoId) && tankId == o.tankId) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return Objects.hash(bulletInfoId);
	}

	@Override
	public int compareTo(ThreatTarget o) {
		// TODO Auto-generated method stub
		if (dodgeBestTick == o.dodgeBestTick) {
			return 0;
		} else if (dodgeBestTick > o.dodgeBestTick) {
			return 1;
		} else {
			return -1;
		}
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("ThreatTarget.tankid[%d]heading[%d]pos[%d,%d]bullet[%d,%d]r[%d]dodgeFinalR[%d]handle[%b]", tankId,tankHeading,tankX,tankY,bulletX,bulletY,bulletR,dodgeFinalR,handled);
	}
}
