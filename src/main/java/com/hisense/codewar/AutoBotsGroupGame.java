package com.hisense.codewar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGame;
import com.hisense.codewar.player.AutoBotsPlayer;
import com.jfinal.kit.PropKit;
/**
 * 团队赛
 * @author hanzheng
 *
 */
public class AutoBotsGroupGame {
	private static final Logger log = LoggerFactory.getLogger(AutoBotsGroupGame.class);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PropKit.use("tank.properties");
		System.out.println("AITank AutoBots Group Game start...");
		String server = PropKit.get("server.ip");
		int port = PropKit.getInt("server.port");
		System.out.println("server is " + server + ":" + port);
		
		final String token1 = PropKit.get("tank1.token");
		final String token2 = PropKit.get("tank2.token");
		final String token3 = PropKit.get("tank3.token");
		AppConfig.init();
		
		TankGame game1 = new TankGame();
		TankGame game2 = new TankGame();
		TankGame game3 = new TankGame();
		
		AutoBotsPlayer player1 = new AutoBotsPlayer();
		AutoBotsPlayer player2 = new AutoBotsPlayer();
		AutoBotsPlayer player3 = new AutoBotsPlayer();
		
		ITtank tank1 = game1.tank_init("10.18.224.205", 22222, token1, player1, null);
		ITtank tank2 = game2.tank_init("10.18.224.205", 22222, token2, player2, null);
		ITtank tank3 = game3.tank_init("10.18.224.205", 22222, token3, player3, null);
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					game1.tank_loop(tank1);
				} catch (Exception e) {
					// TODO: handle exception
					log.error(e.toString());
				}

			}
		}).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					game2.tank_loop(tank2);
				} catch (Exception e) {
					// TODO: handle exception
					log.error(e.toString());
				}

			}
		}).start();
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
