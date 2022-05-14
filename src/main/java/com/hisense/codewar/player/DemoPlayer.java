package com.hisense.codewar.player;

import java.util.List;
import java.util.Random;

import javax.management.MBeanAttributeInfo;

import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hisense.codewar.ITtank;
import com.hisense.codewar.MainGroupGame;
import com.hisense.codewar.TankGameActionType;
import com.hisense.codewar.TankGameInfo;
import com.hisense.codewar.TankGamePlayInterface;
import com.hisense.codewar.TankGameState;
import com.hisense.codewar.TankMapProjectile;
import com.hisense.codewar.utils.CombatWarning;
import com.hisense.codewar.utils.CombatWarning.Suggestion;
import com.hisense.codewar.utils.Utils;
import com.jfinal.log.Log;

public class DemoPlayer implements TankGamePlayInterface {
	public static void main(String[] args) {
		int i = -90;

		int j = Utils.formatAngle(i);

		System.out.println(j);
	}

	private static final Logger log = LoggerFactory.getLogger(DemoPlayer.class);

	private int heading = 0;
	private int id = 0;
	int nowx, nowy = 0;
	private Suggestion mSuggestion = null;
	private CombatWarning mCombatWarning = new CombatWarning();
	private Random mRandom=new Random();
	@Override
	public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles) {
		// TODO Auto-generated method stub
		// log.debug("updatemap");
		id = tank.getId();
		nowx = 0;
		nowy = 0;
		TankGameState state = tank.getState();
		for (TankGameInfo t : tanks) {
			if (t.getId() == id) {
				nowx = t.x;
				nowy = t.y;
				heading = t.getR();
				mCombatWarning.updateMyPos(id,nowx, nowy, heading);
				break;
			}
		}
		mCombatWarning.updateTanksPos(tanks);
		// print1(tanks);
		// print2(projectiles);
		mSuggestion = mCombatWarning.analyze(projectiles);
	}

	@Override
	public void gametick(ITtank tank) {
		// TODO Auto-generated method stub
		log.debug("gametick");
		if (mSuggestion != null) {
			int action = mSuggestion.action;
			if (action == Suggestion.DODGE) {
				int r = mSuggestion.r;
				int h = mSuggestion.heading;
				
				//r = mRandom.nextInt(90);
				tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, r);
				tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, r);
				tank.tank_action(TankGameActionType.TANK_ACTION_ROTATE, h);
				log.debug(String.format("##Dodge###r[%d]h[%d]", r, h));
			}
		}
		
//		tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, 0);
//		tank.tank_action(TankGameActionType.TANK_ACTION_MOVE, 0);

	}

	protected void print1(List<TankGameInfo> list) {
		for (TankGameInfo info : list) {
			log.debug(info.toString());
		}
	}

	protected void print2(List<TankMapProjectile> list) {
		for (TankMapProjectile p : list) {
			log.debug(p.toString());
			if (p.tankid == id) {
				int bulletDistance = Utils.getDistance(p.x, p.y, nowx, nowy);
				log.debug(String.format("bullet[%d,%d]r[%d]distance[%d]-->tankid[%d]", p.x, p.y, p.r, bulletDistance,
						id));
			}
		}
	}

	@Override
	public void onstart(int i) {
		// TODO Auto-generated method stub
		if (i == 3) {
			log.debug("********************start********************");
		} else if (i == 2) {
			log.debug("######################start######################");
		}
	}
}
