package com.hisense.codewar;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hisense.codewar.demo.DemoShooterGame;
import com.jfinal.kit.PropKit;

public class MainGroupGame implements TankGamePlayInterface {
	private static final Logger log = LoggerFactory.getLogger(MainGroupGame.class);
	public static void main(String[] args) {
		PropKit.use("tank.properties");
		System.out.println("AITank start...");
		String server = PropKit.get("server.ip");
		int port = PropKit.getInt("server.port");
		System.out.println("server is " + server + ":" + port);
		final String token1 = PropKit.get("tank1.token");
		log.info("token is " + token1);
		TankGame game1 = new TankGame();
		TankGame game2 = new TankGame();
		TankGame game3 = new TankGame();
		DemoShooterGame player1 = new DemoShooterGame();
		DemoShooterGame player2 = new DemoShooterGame();
		DemoShooterGame player3 = new DemoShooterGame();
	}

	@Override
	public void updatemap(ITtank tank, List<TankGameInfo> tanks, List<TankMapProjectile> projectiles) {
		// TODO Auto-generated method stub

	}

	@Override
	public void gametick(ITtank tank) {
		// TODO Auto-generated method stub

	}

}
