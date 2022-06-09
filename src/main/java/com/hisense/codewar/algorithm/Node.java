package com.hisense.codewar.algorithm;

public class Node {
	public int x;
	public int y;
	public boolean b;

	public Node(int X, int Y, boolean B) {
		x = X;
		y = Y;
		b = B;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("Node[%d,%d]b[%b]", x,y,b);
	}
}
