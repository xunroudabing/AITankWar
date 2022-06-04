package com.hisense.codewar.wave;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.data.CombatEnemyDatabase;
import com.hisense.codewar.data.CombatRealTimeDatabase;
import com.hisense.codewar.data.EnemyCombatData;
import com.hisense.codewar.data.LimitedQueue;
import com.hisense.codewar.data.EnemyCombatData.MovementTrack;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;

/**
 * 波统计射击算法
 * 
 * @author hanzheng
 *
 */
public class WaveSurfing {
	private int mTick;
	private List<Wave> mWaveList;
	private Map<Integer, LimitedQueue<Wave>> mWaveDataBase;
	private CombatEnemyDatabase mEnemyDatabase;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(WaveSurfing.class);

	public WaveSurfing(CombatRealTimeDatabase database, CombatEnemyDatabase enemyDatabase) {
		mDatabase = database;
		mEnemyDatabase = enemyDatabase;
		mWaveDataBase = new HashMap<>();
		mWaveList = new ArrayList<Wave>();
	}

	public void scan(int tick) {
		try {
			mTick = tick;
			loopWavesAndHitTest(tick);		
			//创建波
			//createWaves(tick);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.toString());
		}

	}

	/*
	 * 獲得最佳匹配射擊角
	 */
	public double getBestMatchFireAngle(int tankid) {
		double aim = 0;
		double distance;

		LimitedQueue<Wave> queue = mWaveDataBase.get(tankid);
		if (queue == null) {
			return aim;
		}
		EnemyCombatData eCombatData = mEnemyDatabase.getEnemyData(tankid);
		if (eCombatData == null) {
			return aim;
		}
		MovementTrack track = eCombatData.trackData;
		double velSeg = track.velSeg;
		double adSeg = track.adSeg;
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int currentAngle = Utils.angleTo(nowX, nowY, track.x, track.y);
		int dis = Utils.distanceTo(nowX, nowY, track.x, track.y);
		// 定義一個匹配，初始化成一個非常大的值
		double maxMatch = Double.POSITIVE_INFINITY;
		Iterator<Wave> iter = queue.iterator();
		while (iter.hasNext()) {
			Wave w = iter.next();
			//匹配信號相似度
			distance = Math.pow(velSeg - w.getVelSeg(), 2) + Math.pow((adSeg - w.getAdSeg()), 2)
					+ Math.pow((w.getDist() - dis) / 200, 2);

			if (distance < maxMatch) {
				maxMatch = distance;
				aim = w.getAngle();
				log.debug(String.format(
						"velSeg[%f,%f]adSeg[%f,%f]dist[%d,%d]distance[%f]aim[%f]currentAngle[%d] %f , %f , %f", velSeg,
						w.getVelSeg(), adSeg, w.getAdSeg(), dis, w.getDist(), distance, aim, currentAngle,
						Math.pow(velSeg - w.getVelSeg(), 2), Math.pow((adSeg - w.getAdSeg()), 2),
						Math.pow((w.getDist() - dis) / 50, 2)));
			}
		}
		log.debug("bestMatch=" + maxMatch + ",aim=" + aim);
		return aim;
	}

	// 测试扫描波是否击中目标
	public void loopWavesAndHitTest(int tick) {
		Iterator<Wave> iterator = mWaveList.iterator();
		while (iterator.hasNext()) {
			Wave wave = (Wave) iterator.next();
			int tankId = wave.getEnemyTankId();
			TankGameInfo enemyTank = mDatabase.getTankById(tankId);
			if (enemyTank == null) {
				continue;
			}
			HitTestResult result = wave.hitTest(tick, enemyTank);
			// 波击中目标
			if (result.ret) {
				wave.setAngle(result.suggestAimAngle);
				insertToDB(tankId, wave);
				iterator.remove();
			}
		}
		// log.debug("loopWavesAndHitTest");
	}

	// 创建波
	public void createWaves(int tick) {
		List<TankGameInfo> enemyTanks = mDatabase.getEnemyTanks();
		for (TankGameInfo tank : enemyTanks) {
			createWave(tank, tick);
		}
	}

	public void createWave(TankGameInfo enemyTank, int tick) {

		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int tankid = mDatabase.getMyTankId();

		EnemyCombatData enemyCombatData = mEnemyDatabase.getEnemyData(enemyTank.id);
		if (enemyCombatData == null) {
			return;
		}
		MovementTrack trackData = enemyCombatData.trackData;
		if (trackData == null) {
			log.debug("track data == null");
			return;
		}

		else if (trackData.absAngle == -1 || trackData.speed == -1) {
			log.debug("speed  == -1");
			return;
		}

		int distance = Utils.distanceTo(nowX, nowY, enemyTank.x, enemyTank.y);

		Wave wave = new Wave();
		wave.setEnemyTankId(enemyTank.id);
		wave.setStartTick(mTick);
		wave.setMyStartPos(new Position(nowX, nowY));
		wave.setAbsBearing(trackData.absAngle);
		wave.setVelSeg(trackData.velSeg);
		wave.setAdSeg(trackData.adSeg);
		wave.setSpeed(trackData.speed);
		wave.setStartTick(tick);
		wave.setDist(distance);

		mWaveList.add(wave);
		// log.debug("createWave END");
	}

	/**
	 * 将波存储至数据库备用
	 * 
	 * @param tankid
	 * @param wave
	 */
	protected void insertToDB(int tankid, Wave wave) {
		boolean existTank = mWaveDataBase.containsKey(tankid);
		if (!existTank) {
			LimitedQueue<Wave> queue = new LimitedQueue<>(AppConfig.WAVE_DB_MAXROWS);
			queue.add(wave);
			mWaveDataBase.put(tankid, queue);
		} else {
			LimitedQueue<Wave> queue = mWaveDataBase.get(tankid);
			queue.add(wave);

		}
	}
}
