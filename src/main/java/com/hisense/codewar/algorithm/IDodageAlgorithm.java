package com.hisense.codewar.algorithm;

import java.util.List;

import com.hisense.codewar.model.Bullet;

public interface IDodageAlgorithm {
	void scan(List<Bullet> bulletList,int nowX,int nowY,int tick);
}
