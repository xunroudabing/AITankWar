package com.hisense.codewar;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.HitInfo;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGame;
import com.hisense.codewar.model.TankGameInfo;
import com.hisense.codewar.model.TankGamePlayInterface;
import com.hisense.codewar.model.TankMapProjectile;
import com.hisense.codewar.player.AutoBotsPlayer;
import com.jfinal.kit.PropKit;

public class MainGroupGame implements TankGamePlayInterface {
	private static final Logger log = LoggerFactory.getLogger(MainGroupGame.class);

	public static void main(String[] args) {
		PropKit.use("tank.properties");
		System.out.println("AITank start...");
		String server = PropKit.get("server.ip");
		int port = PropKit.getInt("server.port");
		System.out.println("server is " + server + ":" + port);
		final String token3 = PropKit.get("tank3.token");
		log.info("token is " + token3);
		AppConfig.init();
		TankGame game3 = new TankGame();

		// DemoPlayer player3 = new DemoPlayer();
		// DemoShooterGame player3 = new DemoShooterGame();
		AutoBotsPlayer player3 = new AutoBotsPlayer();
		ITtank tank3 = game3.tank_init("10.18.224.205", 22222, token3, player3, null);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					game3.tank_loop(tank3);
				} catch (Exception e) {
					// TODO: handle exception
					log.error(e.toString());
				}

			}
		}).start();
	}

	@Override
	public void gametick(ITtank tank) {
		// TODO Auto-generated method stub
		log.debug("gametick");

	}

	@Override
	public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles, int r,
			List<HitInfo> hits) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gamestart(ITtank tank) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameend(ITtank tank) {
		// TODO Auto-generated method stub
		
	}

}
