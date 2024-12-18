package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.UserPoint;

interface PointService {

	// TODO: facade pattern 변경하여 point history 분리할 것
	UserPoint charge(long userId, long chargeAmount);

	UserPoint use(long userId, long amount);
}
