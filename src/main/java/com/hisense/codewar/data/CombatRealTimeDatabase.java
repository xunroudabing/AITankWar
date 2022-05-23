package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.Bullet;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankMapBlock;
import com.hisense.codewar.model.TankMapProjectile;
import com.hisense.codewar.utils.PoisionCircleUtils;
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
	}

	private PoisionCircleUtils mPoisionUtils;
	private int mThreadBulletsCount;
	private int mTankId;
	private int mNowX;
	private int mNowY;
	private int mHeading;
	private int[] mFriendTanksID;
	private List<TankMapProjectile> mProjectiles;
	private List<TankGameInfo> mAllTanks;
	private List<TankMapBlock> mBlocks;

	private List<TankGameInfo> mFriendTanks;
	private List<Bullet> mBullets;

	private static final Logger log = LoggerFactory.getLogger(CombatRealTimeDatabase.class);

	public CombatRealTimeDatabase() {
		PropKit.use("tank.properties");
		mAllTanks = new ArrayList<>();
		mProjectiles = new ArrayList<>();
		mFriendTanks = new ArrayList<>();
		mBullets = new ArrayList<>();
		mBlocks = new ArrayList<TankMapBlock>();
		mPoisionUtils = new PoisionCircleUtils();
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
		mBlocks.clear();
		mPoisionUtils.reset();
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
		mPoisionUtils.updateR(r);
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

	public TankGameInfo getLeader() {
		List<TankGameInfo> friends = getFriendTanks();
		int tankid = getMyTankId();
		Collections.sort(friends);
		// 如果没有队友，自己就是leader,id最小的是leader
		if (friends.size() > 1) {
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

	public boolean inBlocks(int x, int y) {
		return Utils.inBlocks(x, y, AppConfig.BLOCK_SIZE, getBlocks(), AppConfig.BLOCK_SIZE);
	}

	public boolean isOutRange(int x, int y) {
		return mPoisionUtils.isOut(x, y);
	}

	public boolean fireInBlocks(int nowX, int nowY, int tx, int ty) {
		return Utils.fireInBlock(nowX, nowY, tx, ty, getBlocks(), AppConfig.BLOCK_SIZE);
	}

	private void loopAllTanks() {
		mFriendTanks.clear();
		for (TankGameInfo tank : mAllTanks) {
			if (tank.id == mTankId) {
				mNowX = tank.x;
				mNowY = tank.y;
				mHeading = tank.r;
				continue;
			}
			if (isFriend(tank.id)) {
				mFriendTanks.add(tank);
				continue;
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
