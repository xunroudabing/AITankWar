package com.hisense.codewar.antigrave;

public class GravePoint {
	private int x;
	private int y;
	private int power;

	public GravePoint(int px, int py, int pPower) {
		x = px;
		y = py;
		power = pPower;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("[GPoint][%d,%d]power[%d]", x, y, power);
	}
}
