package com.hisense.codewar.algorithm;

import java.util.Objects;

public class Node implements Cloneable {
	public int x;
	public int y;
	public boolean b;

	public Node(int X, int Y, boolean B) {
		x = X;
		y = Y;
		b = B;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof Node) {
			Node node = (Node) obj;
			return node.x == this.x && node.y == this.y;
		}
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return Objects.hash(this.x, this.y);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("Node[%d,%d]b[%b]", x, y, b);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		Node ret = (Node) super.clone();
		ret.x = this.x;
		ret.y = this.y;
		ret.b = this.b;
		return ret;

	}
}
