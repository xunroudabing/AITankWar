package com.hisense.codewar.algorithm;

import java.util.List;

import com.hisense.codewar.model.Position;

public interface ITrackingAlgorithm {

	List<Position> track(int nowX, int nowY, int targetX, int targetY, int tick);

	List<Position> antitrack(int nowX, int nowY, int targetX, int targetY, int tick);
}
