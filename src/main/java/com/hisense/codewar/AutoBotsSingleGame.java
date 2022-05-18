package com.hisense.codewar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGame;
import com.hisense.codewar.player.AutoBotsPlayer;
import com.jfinal.kit.PropKit;
//单人赛
public class AutoBotsSingleGame {
	private static final Logger log = LoggerFactory.getLogger(AutoBotsSingleGame.class);
	public static void main(String[] args) {
		PropKit.use("tank.properties");
		System.out.println("AITank AutoBots Single Game start...");
		String server = PropKit.get("server.ip");
		int port = PropKit.getInt("server.port");
		System.out.println("server is " + server + ":" + port);
		final String token3 = PropKit.get("tank3.token");
		log.info("token is " + token3);
		AppConfig.init();
		TankGame game3 = new TankGame();

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
}
