package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import java.util.List;

public interface PointService {

	UserPoint getUserPoint(long userId);

	UserPoint charge(long userId, long chargeAmount);

	UserPoint use(long userId, long amount);

	List<PointHistory> selectAllByUserId(long id);
}

