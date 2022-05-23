package com.hisense.codewar.algorithm;

import com.hisense.codewar.model.Position;

public interface ITrackingAlgorithm {

	Position track(int nowX, int nowY, int targetX, int targetY, int tick);

	Position antitrack(int nowX, int nowY, int targetX, int targetY, int tick);
}
