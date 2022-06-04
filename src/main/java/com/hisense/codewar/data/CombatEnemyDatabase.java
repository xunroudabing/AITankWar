package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.data.EnemyCombatData.MovementTrack;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.utils.Utils;

/**
 * 敌人运行轨迹数据库
 * 
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
		// 扫描敌人位置
		List<TankGameInfo> list = mDatabase.getEnemyTanks();
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		Iterator<TankGameInfo> iterator = list.iterator();
		while (iterator.hasNext()) {
			TankGameInfo tank = (TankGameInfo) iterator.next();
			// 读取历史记录，查看有无此坦克数据
			EnemyCombatData data = getEnemyData(tank.id);
			int distance = Utils.distanceTo(nowX, nowY, tank.x, tank.y);
			if (data != null) {
				// 有数据，更新数据,上一次记录
				MovementTrack lastTrack = data.trackData;

				// 本次记录
				MovementTrack currentTrack = new MovementTrack();
				currentTrack.x = tank.x;
				currentTrack.y = tank.y;
				currentTrack.absAngle = -1;
				currentTrack.speed = -1;
				currentTrack.velSeg = -1;
				currentTrack.adSeg = -1;
				currentTrack.dist = distance;
				currentTrack.tick = tick;
				// 计算角度速度，更新数据
				data.trackData = compareGetMovement(currentTrack, lastTrack);
				// log.debug("update " + currentTrack.toString());
				try {
					// 保存至历史记录
					if (data.trackData.speed != -1 && data.trackData.velSeg != -1 && data.trackData.adSeg != -1) {
						MovementTrack hisTrack = data.trackData.clone();
						data.addToHistroy(hisTrack);
						log.debug("addToHis " + hisTrack.toString());
					}
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					log.error(e.toString());
				}

			} else {
				// 无记录，新增数据
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
				track.dist = distance;
				track.tick = tick;
				newData.trackData = track;
				mEnemyCombatHistoryDatas.add(newData);
				log.debug("add");
			}

		}
	}

	// 根据上一个tick算当前tick的角度和速度分量
	protected MovementTrack compareGetMovement(MovementTrack currentTrack, MovementTrack lastTrack) {
		// log.debug("currentTrack=" + currentTrack.toString() + ",lastTrack=" +
		// lastTrack.toString());
		// 连续数据，计算速度与角度
		if ((currentTrack.tick - lastTrack.tick) == 1) {
			int dis = Utils.distanceTo(currentTrack.x, currentTrack.y, lastTrack.x, lastTrack.y);
			// 速度的最大值应该是9，此处过滤speed为10的情况
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

	// 用于匹配段的长度
	private static final int MATCH_LENGHT = 10;

	protected int getMatchIndex(int tankid) {
		EnemyCombatData data = getEnemyData(tankid);
		if (data == null) {
			return -1;
		}

		List<MovementTrack> tracks = data.historyTracks;
		int totalSize = tracks.size();
		if (totalSize <= MATCH_LENGHT * 10) {
			return -1;
		}
		int currentIndex = totalSize - 1;
		double beatSimilarity = Double.POSITIVE_INFINITY;
		int matchIndex = 0;
		// 这里取i<currentFrame-100是为了避免比较样本和被比较样本重复
		// 和留取足够的节点给递推未来坐标用
		for (int i = MATCH_LENGHT; i < currentIndex - MATCH_LENGHT; i++) {
			// 取10个样本节点计算相似度
			double similarity = 0;
			for (int j = 1; j <= MATCH_LENGHT; j++) {
				double compare_velSeg = tracks.get(i - j).velSeg;
				double compare_adSeg = tracks.get(i - j).adSeg;

				double current_velSeg = tracks.get(currentIndex - j).velSeg;
				double current_adSeg = tracks.get(currentIndex - j).adSeg;

				// double compare_dist = tracks.get(i - j).dist;
				// double current_dist = tracks.get(currentIndex - j).dist;
				// 相似度
				similarity += Math.pow(current_velSeg - compare_velSeg, 2) + Math.pow(current_adSeg - compare_adSeg, 2);
				// 相似度可以加入 与敌人的距离、子弹距离方向等对比数据
				// similarity += Math.pow((current_dist - compare_dist) / 200, 2);

				// similarity += Math.abs(velocityRecord[i - j] - velocityRecord[currentIndex -
				// j]);
				// similarity += Math.abs(headingRecord[i - j] - headingRecord[currentIndex -
				// j]);

			}
			// 记录最相似的相似度，以及对应的记录节点下标
			if (similarity < beatSimilarity) {
				matchIndex = i;
				beatSimilarity = similarity;
			}
		}
		return matchIndex;

	}

	public Position guessPositionByPattern(int tankid) {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();
		int meId = mDatabase.getMyTankId();
		// 预测位置
		TankGameInfo enemyTank = mDatabase.getTankById(tankid);
		Position oriPosition = new Position(enemyTank.x, enemyTank.y);
		Position guessPosition = new Position(enemyTank.x, enemyTank.y);
		int matchIndex = getMatchIndex(tankid);
		// 无匹配数据
		if (matchIndex < 0) {
			return oriPosition;
		}
		int dis = Utils.distanceTo(nowX, nowY, enemyTank.x, enemyTank.y);
		// 距离过近不预测
		if (dis <= 60) {
			return oriPosition;
		}
		EnemyCombatData data = getEnemyData(tankid);
		// 无数据，不预测
		if (data == null) {
			return oriPosition;
		}
		List<MovementTrack> tracks = data.historyTracks;
		// 当前状态
		MovementTrack currentTrack = data.trackData;
		if (currentTrack != null) {
			// 当前静止目标不预测
			if (currentTrack.velSeg == 0 && currentTrack.velSeg == 0) {
				return oriPosition;
			}
		}
		int currentIndex = tracks.size() - 1;
		int time = 0;
		while (matchIndex + time < currentIndex) {
			double distance = Utils.distanceTo(nowX, nowY, guessPosition.x, guessPosition.y);
			if (((distance - AppConfig.TARGET_RADIUS) / AppConfig.BULLET_SPEED) <= time) {
				break;
			}

			int guessX = (int) (guessPosition.x + tracks.get(matchIndex + time).adSeg);
			int guessY = (int) (guessPosition.y + tracks.get(matchIndex + time).velSeg);
			guessPosition = new Position(guessX, guessY);
			log.debug(String.format("[Patter-Guess-Loop]me[%d]enemyid[%d]oriPos[%d,%d]guessPos[%d,%d]guessTime[%d]", meId,tankid,
					enemyTank.x, enemyTank.y, guessPosition.x, guessPosition.y, time));
			time++;
		}
		// time过大不预测
		if (time > 40) {
			return oriPosition;
		}
		log.info(String.format("[Patter-Guess-Final]enemyid[%d]oriPos[%d,%d]guessPos[%d,%d]guessTime[%d]", tankid,
				enemyTank.x, enemyTank.y, guessPosition.x, guessPosition.y, time));
		return guessPosition;
	}

	public Position guessPosition(int tankid, int when) {
		EnemyCombatData data = getEnemyData(tankid);
		if (data != null) {
			int x = data.trackData.x;
			int y = data.trackData.y;
			double velSeg = data.trackData.velSeg;
			double adSeg = data.trackData.adSeg;
			if (velSeg == -1 || adSeg == -1) {
				return null;
			}
			int speed = data.trackData.speed;
			int newY = (int) (y + velSeg * when);
			int newX = (int) (x + adSeg * when);

			return new Position(newX, newY);

		}
		return null;
	}

	public Position guessPosition(int tankid) {
		int nowX = mDatabase.getNowX();
		int nowY = mDatabase.getNowY();

		int bulletTick = 0;
		// 敌人移动时间
		int when = 0;
		Position enenmyPosition = null;

		do {
			when++;
			// when时间后敌人的位置
			enenmyPosition = guessPosition(tankid, when);
			if (enenmyPosition == null) {
				break;
			}
			int dis = Utils.distanceTo(nowX, nowY, enenmyPosition.x, enenmyPosition.y);
			// 计算子弹到达时间,子弹能够达到，迭代结束
			bulletTick = dis / AppConfig.BULLET_SPEED;
			log.debug(String.format("[Bullet-Loop]bulletTick[%d]when[%d]enemyPos[%d,%d]", bulletTick, when,
					enenmyPosition.x, enenmyPosition.y));

		} while (bulletTick >= when || when > 7);

		return enenmyPosition;
	}
}
