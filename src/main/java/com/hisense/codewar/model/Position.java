package com.hisense.codewar.model;

public class Position {
	public int x;

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

	public int y;
	public Position() {
		
	}
	public Position(int mx, int my) {
		x = mx;
		y = my;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("Position[%d,%d]", x, y);
	}
}
