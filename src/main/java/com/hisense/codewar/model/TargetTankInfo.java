package com.hisense.codewar.model;

/**
 * 战斗目标
 * 
 * @author hanzheng
 *
 */
public class TargetTankInfo extends TankGameInfo implements Comparable<TargetTankInfo> {
	/**
	 * 攻击优先级
	 */
	public int priority;

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public TargetTankInfo(int id, int x, int y, int r) {
		super(id, x, y, r);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(TargetTankInfo o) {
		// TODO Auto-generated method stub
		if (getPriority() == o.getPriority()) {
			return 0;
		} else if (getPriority() > o.getPriority()) {
			return 1;
		} else {
			return -1;
		}
	}

}
