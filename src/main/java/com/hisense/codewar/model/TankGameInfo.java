package com.hisense.codewar.model;

import java.util.Objects;

public class TankGameInfo implements Comparable<TankGameInfo> {
    public int id;
    public int x;
    public int y;
    public int r;
    public int hp;

    public TankGameInfo(int id, int x, int y, int r, int hp){
        this.id = id;
        this. x = x;
        this. y = y;
        this. r = r;
        this.hp = hp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }
    
	@Override
	public String toString() {
		return String.format("tankId[%d]postion[%d,%d]r[%d]hp[%d]", id, x, y, r,hp);
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof TankGameInfo) {
			TankGameInfo o = (TankGameInfo) obj;
			return getId() == o.id;
		}
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return Objects.hash(getId());
	}

	@Override
	public int compareTo(TankGameInfo o) {
		// TODO Auto-generated method stub
		return getId() - o.getId();
	}

}
