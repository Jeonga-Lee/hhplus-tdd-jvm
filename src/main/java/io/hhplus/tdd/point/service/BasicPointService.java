package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPointDomain;
import org.springframework.stereotype.Service;

@Service
public class BasicPointService implements PointService {

	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;

	public BasicPointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
		this.userPointTable = userPointTable;
		this.pointHistoryTable = pointHistoryTable;
	}

	private UserPointDomain getUserPointDomain(long userId) {
		UserPoint userPoint = userPointTable.selectById(userId);
		return new UserPointDomain(userPoint);
	}

	public UserPoint charge(long userId, long chargeAmount) {
		// UserPoint를 조회하고, 이를 UserPointDomain에 래핑
		UserPointDomain userPointDomain = getUserPointDomain(userId);
		UserPoint updatedUserPoint = userPointDomain.charge(chargeAmount);

		pointHistoryTable.insert(userId, chargeAmount, TransactionType.CHARGE,
			System.currentTimeMillis());

		return userPointTable.insertOrUpdate(userId, updatedUserPoint.point());
	}

	public UserPoint use(long userId, long amount) {
		UserPointDomain userPointDomain = getUserPointDomain(userId);
		UserPoint updatedUserPoint = userPointDomain.use(amount);

		pointHistoryTable.insert(userId, amount, TransactionType.USE,
			System.currentTimeMillis());

		return userPointTable.insertOrUpdate(userId, updatedUserPoint.point());
	}
}