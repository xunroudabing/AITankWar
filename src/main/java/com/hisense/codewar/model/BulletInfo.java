package com.hisense.codewar.model;

import java.util.ArrayList;
import java.util.List;
/**
 *  记录敌人弹道数据
 * @author hanzheng
 *
 */
public class BulletInfo {

	private List<Position> mBulletRecord;
	private int tankId;

	public BulletInfo() {
		mBulletRecord = new ArrayList<Position>();
	}

	public List<Position> getBulletRecord() {
		return mBulletRecord;
	}

	public void setBulletRecord(List<Position> mBulletRecord) {
		this.mBulletRecord = mBulletRecord;
	}

	public int getTankId() {
		return tankId;
	}

	public void setTankId(int tankId) {
		this.tankId = tankId;
	}

}
