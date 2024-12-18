package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPointDomain;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;

@Service
public class LockPointService implements PointService {

	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;

	private final ReentrantLock lock = new ReentrantLock();

	public LockPointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
		this.userPointTable = userPointTable;
		this.pointHistoryTable = pointHistoryTable;
	}

	public UserPoint charge(long userId, long chargeAmount) {
		lock.lock();

		try {
			UserPointDomain userPointDomain = getUserPointDomain(userId);
			UserPoint updatedUserPoint = userPointDomain.charge(chargeAmount);

			pointHistoryTable.insert(userId, chargeAmount, TransactionType.CHARGE,
				System.currentTimeMillis());

			return userPointTable.insertOrUpdate(userId, updatedUserPoint.point());
		} finally {
			lock.unlock();
		}
	}

	public UserPoint use(long userId, long amount) {
		lock.lock();

		try {
			UserPointDomain userPointDomain = getUserPointDomain(userId);
			UserPoint updatedUserPoint = userPointDomain.use(amount);

			pointHistoryTable.insert(userId, amount, TransactionType.USE,
				System.currentTimeMillis());

			return userPointTable.insertOrUpdate(userId, updatedUserPoint.point());
		} finally {
			lock.unlock();
		}
	}

	private UserPointDomain getUserPointDomain(long userId) {
		UserPoint userPoint = userPointTable.selectById(userId);
		return new UserPointDomain(userPoint);
	}

}