package com.hisense.codewar.data;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankMapProjectile;
import com.hisense.codewar.player.DemoPlayer;
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

	private int mTankId;
	private int mNowX;
	private int mNowY;
	private int mHeading;
	private int[] mFriendTanksID;
	private List<TankMapProjectile> mProjectiles;
	private List<TankGameInfo> mAllTanks;
	private List<TankGameInfo> mFriendTanks;

	private static final Logger log = LoggerFactory.getLogger(CombatRealTimeDatabase.class);

	public CombatRealTimeDatabase() {
		PropKit.use("tank.properties");
		mAllTanks = new ArrayList<>();
		mProjectiles = new ArrayList<>();
		mFriendTanks = new ArrayList<>();
		initFriendTanks();
	}

	public void reset() {
		mAllTanks.clear();
		mProjectiles.clear();
	}

	public void setMyTankId(int tankid) {
		mTankId = tankid;
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

	private boolean isFriend(int tankid) {
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
