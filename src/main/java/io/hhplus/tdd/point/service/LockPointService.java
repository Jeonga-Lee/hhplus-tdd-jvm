package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.domain.TransactionType;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;

@Service
public class LockPointService implements PointService {

	private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;

	public LockPointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
		this.userPointTable = userPointTable;
		this.pointHistoryTable = pointHistoryTable;
	}

	private ReentrantLock getLockForUser(long userId) {
		return userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
	}

	@Override
	public UserPoint getUserPoint(long userId) {
		return userPointTable.selectById(userId);
	}

	@Override
	public UserPoint charge(long id, long chargeAmount) {
		validatePoint(chargeAmount);

		ReentrantLock userLock = getLockForUser(id);
		userLock.lock();

		try {
			UserPoint userPoint = getUserPoint(id);
			validateMaxPoint(userPoint.point(), chargeAmount);

			UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id,
				userPoint.point() + chargeAmount);

			pointHistoryTable.insert(id, chargeAmount, TransactionType.CHARGE,
				System.currentTimeMillis());

			return updatedUserPoint;
		} finally {
			userLock.unlock();
		}
	}


	@Override
	public UserPoint use(long id, long amount) {
		validatePoint(amount);

		ReentrantLock userLock = getLockForUser(id);
		userLock.lock();

		try {
			UserPoint userPoint = getUserPoint(id);
			validateExistPoint(userPoint.point(), amount);

			UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id,
				userPoint.point() - amount);

			pointHistoryTable.insert(id, amount, TransactionType.USE,
				System.currentTimeMillis());

			return updatedUserPoint;
		} finally {
			userLock.unlock();
		}
	}

	@Override
	public List<PointHistory> selectAllByUserId(long id) {
		return pointHistoryTable.selectAllByUserId(id);
	}

	private void validateExistPoint(long point, long amount) {
		if (point - amount < 0) {
			throw new IllegalArgumentException(
				"포인트가 부족합니다. 현재 " + point + "포인트 보유 중 입니다.");
		}
	}

	private void validateMaxPoint(long point, long chargeAmount) {
		final long MAX_POINT = 1_000_000;
		if (point + chargeAmount > MAX_POINT) {
			long availablePoint = MAX_POINT - point;
			throw new IllegalArgumentException("남은 충전 한도는 " + availablePoint + " 포인트 입니다.");
		}
	}

	private void validatePoint(long chargeAmount) {
		if (chargeAmount <= 0) {
			throw new IllegalArgumentException("최소 100 포인트 이상 입력해주세요.");
		}
		if (chargeAmount % 100 > 0) {
			throw new IllegalArgumentException("100 포인트 단위로 충전 및 사용이 가능합니다.");
		}
	}
}
