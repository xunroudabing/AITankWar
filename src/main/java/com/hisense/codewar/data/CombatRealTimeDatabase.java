package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.Position;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankMapBlock;
import com.hisense.codewar.model.TankMapProjectile;
import com.hisense.codewar.utils.Utils;
import com.jfinal.kit.PropKit;

/**
 * 战斗实时数据库
 * 
 * @author hanzheng
 *
 */
public class CombatRealTimeDatabase {
	public static void main(String[] args) {
		CombatRealTimeDatabase database = new CombatRealTimeDatabase();
		boolean ret = true || false;
		System.out.println(ret);
	}

	private int[][] mMapNodeArrys;
	private Map mMap;
	private int minX;
	private int maxX;
	private int minY;
	private int maxY;
	// 毒圈半径
	private int radius;
	private int battleFieldWidth;
	private int battleFieldHeight;

	private int mThreadBulletsCount;
	private int mTankId;
	private int mNowX;
	private int mNowY;
	private int mHeading;
	private int[] mFriendTanksID;
	private List<TankMapProjectile> mProjectiles;
	private List<TankGameInfo> mAllTanks;
	private List<TankMapBlock> mBlocks;

	private List<TankGameInfo> mEnemyTanks;
	private List<TankGameInfo> mFriendTanks;
	private List<Bullet> mBullets;
	private List<Bullet> mToDoList;

	private static final Logger log = LoggerFactory.getLogger(CombatRealTimeDatabase.class);

	public CombatRealTimeDatabase() {
		PropKit.use("tank.properties");
		mAllTanks = new ArrayList<>();
		mProjectiles = new ArrayList<>();
		mFriendTanks = new ArrayList<>();
		mEnemyTanks = new ArrayList<>();
		mBullets = new ArrayList<>();
		mToDoList = new ArrayList<Bullet>();
		mBlocks = new ArrayList<TankMapBlock>();

		minX = 0;
		maxX = AppConfig.MAP_WITH;
		minY = 0;
		maxY = AppConfig.MAP_HEIGHT;
		mMap = new Map();
		mMap.minX = minX;
		mMap.maxX = maxX;
		mMap.minY = 0;
		mMap.maxY = maxY;
		mMapNodeArrys = new int[1601][901];
		initFriendTanks();
	}

	public List<TankGameInfo> getAllTanks() {
		return mAllTanks;
	}

	public void reset() {
		mAllTanks.clear();
		mProjectiles.clear();
		mBullets.clear();
		mFriendTanks.clear();
		mEnemyTanks.clear();
		mBlocks.clear();
		minX = 0;
		maxX = AppConfig.MAP_WITH;
		minY = 0;
		maxY = AppConfig.MAP_HEIGHT;
		radius = AppConfig.MAP_WITH / 2;
		mMap.minX = minX;
		mMap.maxX = maxX;
		mMap.minY = 0;
		mMap.maxY = maxY;
		mMapNodeArrys = new int[1601][901];
	}

	public List<Bullet> getToDoList() {
		return mToDoList;
	}

	public void setToDoList(List<Bullet> list) {
		mToDoList.clear();
		mToDoList.addAll(list);
	}

	public int getThreatBulletsCount() {
		return mThreadBulletsCount;
	}

	public void setThreatBulletsCount(int count) {
		mThreadBulletsCount = count;
	}

	public List<TankMapBlock> getBlocks() {
		return mBlocks;
	}

	public void setBlocks(List<TankMapBlock> blocks) {
		mBlocks.clear();
		mBlocks.addAll(blocks);
		for (TankMapBlock block : blocks) {
			log.debug(block.toString());
		}

		mMapNodeArrys = new int[1601][901];
		for (TankMapBlock item : getBlocks()) {
			for (int x = item.x - 20; x <= item.x + 20; x++) {
				for (int y = item.y - 20; y <= item.y + 20; y++) {
					mMapNodeArrys[x][y] = -1;
				}
			}
		}
	}

	public void setMyTankId(int tankid) {
		mTankId = tankid;
	}

	/**
	 * 刷新毒圈半径
	 * 
	 * @param r
	 */
	public void updatePoisionR(int r) {
		radius = r;
		int xSize = (AppConfig.MAP_WITH - radius * 2) / 2;
		int ySize = (xSize * 9 / 16);

		minX = xSize;
		maxX = AppConfig.MAP_WITH - xSize;
		minY = ySize;
		maxY = AppConfig.MAP_HEIGHT - ySize;

		battleFieldWidth = radius * 2;
		battleFieldHeight = battleFieldWidth * 9 / 16;

		mMap.minX = minX;
		mMap.maxX = maxX;
		mMap.minY = minY;
		mMap.maxY = maxY;

		log.debug(String.format("[Map][%d,%d]-[%d,%d]-[%d,%d]-[%d,%d]width[%d]height[%d]", minX, minY, minX, maxY, maxX,
				maxY, maxX, minY, battleFieldWidth, battleFieldHeight));
	}

	// 中心点
	public Position getMiddlePostion() {
		int x = Math.abs(maxX - minX) / 2;
		int y = Math.abs(maxY - minY) / 2;
		return new Position(x, y);
	}

	public void updateAllTanks(List<TankGameInfo> list) {
		mAllTanks.clear();
		mAllTanks.addAll(list);
		loopAllTanks();
	}

	public void updateProjectiles(List<TankMapProjectile> list) {
		mProjectiles.clear();
		mProjectiles.addAll(list);
	}

	public List<TankGameInfo> getFriendTanks() {
		return mFriendTanks;
	}

	public List<TankMapBlock> getFriendAsBlocks() {
		List<TankMapBlock> list = new ArrayList<>();
		for (TankGameInfo tank : mFriendTanks) {
			TankMapBlock block = new TankMapBlock(tank.x, tank.y);
			list.add(block);
		}
		return list;
	}

	public List<TankGameInfo> getEnemyTanks() {
		return mEnemyTanks;
	}

	public TankGameInfo getFriend(int tankId) {
		for (TankGameInfo tank : mFriendTanks) {
			if (tank.id == tankId) {
				return tank;
			}
		}
		return null;
	}

	public TankGameInfo getLeader() {
		List<TankGameInfo> friends = getFriendTanks();
		int tankid = getMyTankId();
		Collections.sort(friends);
		// 如果没有队友，自己就是leader,id最小的是leader
		if (friends.size() >= 1) {
			tankid = Math.min(tankid, friends.get(0).id);
		}
		return getTankById(tankid);
	}

	public boolean isLeader() {
		TankGameInfo info = getLeader();
		if (info != null) {
			return info.id == mTankId;
		}
		return false;
	}

	public int[][] getMapNodeArrays() {

		return mMapNodeArrys;
	}

	public boolean isAttackMe(int tankid) {
		for (Bullet bullet : mBullets) {
			if (bullet.tankid == tankid) {
				return true;
			}
		}
		return false;
	}

	public List<Bullet> getBullets() {
		return mBullets;
	}

	public int getNowX() {
		return mNowX;
	}

	public int getNowY() {
		return mNowY;
	}

	public int getHeading() {
		return mHeading;
	}

	public int getMyTankId() {
		return mTankId;
	}

	public List<TankMapProjectile> geTankMapProjectiles() {
		return mProjectiles;
	}

	public TankGameInfo getTankById(int tankid) {
		TankGameInfo ret = null;
		for (TankGameInfo info : mAllTanks) {
			if (info.id == tankid) {
				ret = info;
				break;
			}
		}
		return ret;
	}
	public boolean inBlocks(int x, int y, int blockSize) {
		// 敌人算障碍物
		for (TankGameInfo tank : mEnemyTanks) {
			if (Utils.inBlock(x, y, blockSize, tank.x, tank.y, blockSize)) {
				return true;
			}
		}
		// 队友也算障碍物
		for (TankGameInfo tank : mFriendTanks) {
			if (Utils.inBlock(x, y, blockSize, tank.x, tank.y, blockSize)) {
				return true;
			}
		}
		return Utils.inBlocks(x, y, blockSize, getBlocks(), blockSize);
	}

	public boolean inBlocks(int x, int y) {
//		// 敌人算障碍物
//		for (TankGameInfo tank : mEnemyTanks) {
//			if (Utils.inBlock(x, y, AppConfig.BLOCK_SIZE, tank.x, tank.y, AppConfig.BLOCK_SIZE)) {
//				return true;
//			}
//		}
//		// 队友也算障碍物
//		for (TankGameInfo tank : mFriendTanks) {
//			if (Utils.inBlock(x, y, AppConfig.BLOCK_SIZE, tank.x, tank.y, AppConfig.BLOCK_SIZE)) {
//				return true;
//			}
//		}
//		return Utils.inBlocks(x, y, AppConfig.BLOCK_SIZE, getBlocks(), AppConfig.BLOCK_SIZE);
		return inBlocks(x, y, AppConfig.BLOCK_SIZE);
	}

	public boolean isNearBorderCantMoveByDistance(int nowX, int nowY, int r, int distance) {
		Position endPosition = Utils.getNextPositionByDistance(nowX, nowY, r, distance);
		return isNearBorderCantMove(endPosition.x, endPosition.y);

	}

	public boolean isOutRangeByDistance(int nowX, int nowY, int r, int distance) {
		Position endPosition = Utils.getNextPositionByDistance(nowX, nowY, r, distance);
		return isOut(endPosition.x, endPosition.y);
	}

	public boolean isNextPointCantReach(int nowX, int nowY, int dstX, int dstY) {
		Position nextPostion = new Position(dstX, dstY);
		// return Utils.isNextPointInBlocks(nowX, nowY, r, distance, getBlocks(),
		// AppConfig.BLOCK_SIZE);
		boolean inBlock = inBlocks(nextPostion.x, nextPostion.y);
		boolean crossBlocks = Utils.isCrossBlock(nowX, nowY, nextPostion.x, nextPostion.y, getBlocks(),
				AppConfig.BLOCK_SIZE);
		boolean crossFriends = Utils.isCrossBlock(nowX, nowY, nextPostion.x, nextPostion.y, getFriendAsBlocks(),
				AppConfig.BLOCK_SIZE);
		return inBlock || crossBlocks || crossFriends;
	}

	public boolean isNextPointCrossBlocks(int nowX, int nowY, int r, int distance) {
		Position nextPostion = Utils.getNextPositionByDistance(nowX, nowY, r, distance);
		// return Utils.isNextPointInBlocks(nowX, nowY, r, distance, getBlocks(),
		// AppConfig.BLOCK_SIZE);
		boolean inBlock = inBlocks(nextPostion.x, nextPostion.y);
		boolean crossBlocks = Utils.isCrossBlock(nowX, nowY, nextPostion.x, nextPostion.y, getBlocks(),
				AppConfig.BLOCK_SIZE);
		boolean crossFriends = Utils.isCrossBlock(nowX, nowY, nextPostion.x, nextPostion.y, getFriendAsBlocks(),
				AppConfig.BLOCK_SIZE);
		return inBlock || crossBlocks || crossFriends;
	}

	// true 射击被阻挡
	public boolean fireInBlocks(int nowX, int nowY, int tx, int ty) {
		boolean crossBlocks = Utils.isCrossBlock(nowX, nowY, tx, ty, getBlocks(), AppConfig.BLOCK_SIZE);
		boolean crossFriends = Utils.isCrossBlock(nowX, nowY, tx, ty, getFriendAsBlocks(), AppConfig.BLOCK_SIZE);
		return crossBlocks || crossFriends;
//		 return Utils.isCrossBlock(nowX, nowY, tx, ty, getBlocks(),
//		 AppConfig.BLOCK_SIZE);

//		int r = Utils.angleTo(nowX, nowY, tx, ty);
//		return Utils.isStopFireByBlock(Double.valueOf(nowX), Double.valueOf(nowY), Double.valueOf(tx),
//				Double.valueOf(ty), Double.valueOf(r), getBlocks());
	}

	public int getBattleFieldWidth() {
		return battleFieldWidth;
	}

	public int getBattleFieldHeight() {
		return battleFieldHeight;
	}

	public Map getMap() {
		return mMap;
	}

	// 在最边上且小于半径，无法移动
	public boolean isNearBorderCantMove(int x, int y) {
		if (x > maxX - AppConfig.TANK_SIZE || x < minX + AppConfig.TANK_SIZE) {
			return true;
		} else if (y > maxY - AppConfig.TANK_SIZE || y < minY + AppConfig.TANK_SIZE) {
			return true;
		}
		return false;
	}

	// 返回毒圈半径
	public int getPoisionR() {
		return radius;
	}

	public boolean isOut(int x, int y) {
		if (x > maxX || x < minX) {
			return true;
		} else if (y > maxY || y < minY) {
			return true;
		}
		return false;
	}

	public boolean inLeft(int x, int y) {
		if (x <= minX + AppConfig.TANK_SIZE) {
			return true;
		}
		return false;
	}

	public boolean inRight(int x, int y) {
		if (x >= maxX - AppConfig.TANK_SIZE) {
			log.debug(String.format("[inRight][%d,%d][Map][%d,%d-%d,%d-%d,%d-%d,%d]", x, y, minX, minY, minX, maxY,
					maxX, maxY, maxX, minY));
			return true;
		}
		return false;
	}

	public boolean inBottom(int x, int y) {
		if (y >= maxY - AppConfig.TANK_WIDTH) {
			return true;
		}
		return false;
	}

	public boolean inTop(int x, int y) {
		if (y <= minY + AppConfig.TANK_WIDTH) {
			return true;
		}
		return false;
	}

	private void loopAllTanks() {
		mFriendTanks.clear();
		mEnemyTanks.clear();
		for (TankGameInfo tank : mAllTanks) {
			if (tank.id == mTankId) {
				mNowX = tank.x;
				mNowY = tank.y;
				mHeading = tank.r;
				continue;
			} else if (isFriend(tank.id)) {
				mFriendTanks.add(tank);
				continue;
			} else {
				mEnemyTanks.add(tank);
			}
		}
	}

	private void initFriendTanks() {
		String arrStr = PropKit.get("group.friendTanks");
		if (arrStr == null) {
			log.warn("[group.friendTanks] in tank.properties cannt be null");
			return;
		}
		if (arrStr.equals("")) {
			log.warn("[group.friendTanks] tank.properties cannt be empty");
			return;
		}
		String[] temp = arrStr.split(",");
		if (temp != null) {
			mFriendTanksID = new int[temp.length];
			for (int i = 0; i < temp.length; i++) {
				String str = temp[i];
				try {
					mFriendTanksID[i] = Integer.parseInt(str);
				} catch (Exception e) {
					// TODO: handle exception
					log.error(e.toString());
				}

			}
		}

	}

	boolean isFriend(int tankid) {
		if (mFriendTanksID == null) {
			log.warn("friends has no data");
			return false;
		}
		for (int i = 0; i < mFriendTanksID.length; i++) {
			int j = mFriendTanksID[i];
			if (tankid == j && tankid != mTankId) {
				return true;
			}

		}
		return false;
	}
}
