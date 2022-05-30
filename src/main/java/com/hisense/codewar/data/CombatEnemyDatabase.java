package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.data.EnemyCombatData.MovementTrack;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;
/**
 * 敌人运行轨迹数据库
 * @author hanzheng
 *
 */
public class CombatEnemyDatabase {
	public static void main(String[] args) {
		int x1 = 200;
		int y1 = 400;

		int x2 = 209;
		int y2 = 409;

		int r = Utils.angleTo(x1, y1, x2, y2);
		int speed = Utils.distanceTo(x1, y1, x2, y2);
		// velSeg = e.getVelocity() * Math. cos (e.getHeadingRadians() - absBearing );
		// adSeg = e.getVelocity() * Math. sin (e.getHeadingRadians() - absBearing );
		int velSeg = (int) (speed * Math.sin(Utils.a2r(r)));
		int adSeg = (int) (speed * Math.cos(Utils.a2r(r)));

		System.out.println("r=" + r + ",speed=" + speed + ",velSeg=" + velSeg + ",adSeg=" + adSeg);
	}
	private int mTick = 0;
	private List<EnemyCombatData> mEnemyCombatHistoryDatas;
	private CombatRealTimeDatabase mDatabase;
	private static final Logger log = LoggerFactory.getLogger(CombatEnemyDatabase.class);
	public CombatEnemyDatabase(CombatRealTimeDatabase database) {
		mEnemyCombatHistoryDatas = new ArrayList<EnemyCombatData>();
		mDatabase = database;
	}
	public void reset() {
		mTick = 0;
		mEnemyCombatHistoryDatas.clear();
	}
	public void scan(int tick) {
		mTick = tick;
		//扫描敌人位置
		List<TankGameInfo> list = mDatabase.getEnemyTanks();
		Iterator<TankGameInfo> iterator = list.iterator();
		while (iterator.hasNext()) {
			TankGameInfo tank = (TankGameInfo) iterator.next();
			// 读取历史记录，查看有无此坦克数据
			EnemyCombatData data = getEnemyData(tank.id);
			if (data != null) {
				//有数据，更新数据,上一次记录
				MovementTrack lastTrack = data.trackData;

				// 本次记录
				MovementTrack currentTrack = new MovementTrack();
				currentTrack.x = tank.x;
				currentTrack.y = tank.y;
				currentTrack.absAngle = -1;
				currentTrack.speed = -1;
				currentTrack.velSeg = -1;
				currentTrack.adSeg = -1;
				currentTrack.tick = tick;
				//计算角度速度，更新数据			
				data.trackData = compareGetMovement(currentTrack, lastTrack);
				log.debug("update " + currentTrack.toString());
			} else {
				//无记录，新增数据
				// 判断队伍
				int teamid = 0;
				EnemyCombatData newData = new EnemyCombatData(tank.id, teamid);
				MovementTrack track = new MovementTrack();
				track.x = tank.x;
				track.y = tank.y;
				track.absAngle = -1;
				track.speed = -1;
				track.velSeg = -1;
				track.adSeg = -1;
				track.tick = tick;
				newData.trackData = track;
				mEnemyCombatHistoryDatas.add(newData);
				log.debug("add");
			}

		}
	}
	
	//根据上一个tick算当前tick的角度和速度分量
	protected MovementTrack compareGetMovement(MovementTrack currentTrack, MovementTrack lastTrack) {
		//log.debug("currentTrack=" + currentTrack.toString() + ",lastTrack=" + lastTrack.toString());
		// 连续数据，计算速度与角度
		if ((currentTrack.tick - lastTrack.tick) == 1) {
			int dis = Utils.distanceTo(currentTrack.x, currentTrack.y, lastTrack.x, lastTrack.y);
			//速度的最大值应该是9，此处过滤speed为10的情况
			int speed = Math.min(dis, 9);
			int r = Utils.angleTo(lastTrack.x, lastTrack.y, currentTrack.x, currentTrack.y);
			// velSeg = e.getVelocity() * Math. cos (e.getHeadingRadians() - absBearing );
			// adSeg = e.getVelocity() * Math. sin (e.getHeadingRadians() - absBearing );
			double velSeg = (speed * Math.sin(Utils.a2r(r)));
			double adSeg = (speed * Math.cos(Utils.a2r(r)));

			currentTrack.speed = speed;
			currentTrack.adSeg = adSeg;
			currentTrack.velSeg = velSeg;
			currentTrack.absAngle = r;
			log.debug(currentTrack.toString());
			return currentTrack;
		}

		return currentTrack;
	}

	protected boolean existEnemyData(int tankid) {
		for (EnemyCombatData data : mEnemyCombatHistoryDatas) {
			if (data.tankid == tankid) {
				return true;
			}
		}
		return false;
	}

	public EnemyCombatData getEnemyData(int tankid) {
		for (EnemyCombatData data : mEnemyCombatHistoryDatas) {
			if (data.tankid == tankid) {
				return data;
			}
		}
		return null;
	}
}
