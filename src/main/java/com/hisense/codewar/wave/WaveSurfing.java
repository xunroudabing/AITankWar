package com.hisense.codewar.wave;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.model.TankGameInfo;

public class WaveSurfing {
	private int mTick;
	private List<Wave> mWaveList;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(WaveSurfing.class);

	public WaveSurfing(CombatRealTimeDatabase database) {
		mDatabase = database;
		mWaveList = new ArrayList<Wave>();
	}

	public void scan(int tick) {
		mTick = tick;

	}

	public void createWave(TankGameInfo enemyTank) {

		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int tankid = mDatabase.getMyTankId();

		Wave wave = new Wave();
		wave.setStartTick(mTick);
		wave.setMyStartPos(null);
	}
}
