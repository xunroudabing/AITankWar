package com.hisense.codewar.data;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombatStatistics {
	public static void main(String[] args) {
		CombatStatistics combatStatistics = new CombatStatistics();
		combatStatistics.show();
	}

	private int hitEnemyCount = 0;
	private int totalFireCount = 0;
	private int hitmeCount = 0;// 被击中次数
	private static final Logger log = LoggerFactory.getLogger(CombatStatistics.class);

	public CombatStatistics() {

	}

	public void reset() {
		hitEnemyCount = 0;
		totalFireCount = 0;
		hitmeCount = 0;
	}

	public void fireCounter() {
		totalFireCount++;
	}

	public void hitCounter() {
		hitEnemyCount++;
	}

	public void hitmeCounter() {
		hitmeCount++;
	}

	public void show() {
		if (totalFireCount == 0) {
			return;
		}
		// double percent = hitEnemyCount * 100 / totalFireCount;
		BigDecimal bigDecimal = BigDecimal.valueOf(hitEnemyCount * 100);
		BigDecimal result = bigDecimal.divide(BigDecimal.valueOf(totalFireCount), 2, BigDecimal.ROUND_HALF_DOWN);
		log.info(String.format("[CombatStatistics]HitEnemy[%d]TotalShoot[%d]HitMe[%d]###HitRate:%s%%###", hitEnemyCount,
				totalFireCount, hitEnemyCount, result.toString()));
	}
}
