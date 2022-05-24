package com.hisense.codewar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGame;
import com.hisense.codewar.player.DecepticonPlayer;
import com.hisense.codewar.player.TrackerPlayer;
import com.jfinal.kit.PropKit;

public class DecepticonSingleGame {
	private static final Logger log = LoggerFactory.getLogger(DecepticonSingleGame.class);

	public static void main(String[] args) {
		PropKit.use("tank.properties");
		System.out.println("AITank AutoBots Single Game start...");
		String server = PropKit.get("server.ip");
		int port = PropKit.getInt("server.port");
		System.out.println("server is " + server + ":" + port);
		final String token4 = PropKit.get("tank4.token");
		// log.info("token is " + token3);
		AppConfig.init();
		TankGame game3 = new TankGame();

		TrackerPlayer player4 = new TrackerPlayer();
		ITtank tank3 = game3.tank_init("10.18.224.205", 22222, token4, player4, null);

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
}
