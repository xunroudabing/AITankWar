package com.hisense.codewar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.config.AppConfig;
import com.hisense.codewar.model.ITtank;
import com.hisense.codewar.model.TankGame;
import com.hisense.codewar.player.AutoBotsPlayer;
import com.hisense.codewar.player.DecepticonPlayer;
import com.jfinal.kit.PropKit;

public class DecepticonGroupGame {
	private static final Logger log = LoggerFactory.getLogger(DecepticonGroupGame.class);
	public static void main(String[] args) {
		PropKit.use("tank.properties");
		System.out.println("App version " + AppConfig.APPVERSION);
		System.out.println("AITank AutoBots Group Game start...");
		String server = PropKit.get("server.ip");
		int port = PropKit.getInt("server.port");
		System.out.println("server is " + server + ":" + port);
		
		final String token4 = PropKit.get("tank4.token");
		final String token5 = PropKit.get("tank5.token");
		final String token6 = PropKit.get("tank6.token");
		AppConfig.init();
		
		TankGame game4 = new TankGame();
		TankGame game5 = new TankGame();
		TankGame game6 = new TankGame();
		
		DecepticonPlayer player4 = new DecepticonPlayer();
		DecepticonPlayer player5 = new DecepticonPlayer();
		DecepticonPlayer player6 = new DecepticonPlayer();
		
		ITtank tank4 = game4.tank_init("10.18.224.205", 22222, token4, player4, null);
		ITtank tank5 = game5.tank_init("10.18.224.205", 22222, token5, player5, null);
		ITtank tank6 = game6.tank_init("10.18.224.205", 22222, token6, player6, null);
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					game4.tank_loop(tank4);
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
					game5.tank_loop(tank5);
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
					game6.tank_loop(tank6);
				} catch (Exception e) {
					// TODO: handle exception
					log.error(e.toString());
				}

			}
		}).start();
	}

}
