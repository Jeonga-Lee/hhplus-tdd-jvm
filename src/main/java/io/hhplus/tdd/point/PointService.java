package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final UserPointTable userPointTable;

    public PointService(UserPointTable userPointTable) {
        this.userPointTable = userPointTable;
    }

    public void charge(long userId, long chargeAmount) {
        validateChargePoint(chargeAmount);

        UserPoint userPoint = getUserPoint(userId);

        validateUserPoint(chargeAmount, userPoint);
        userPointTable.insertOrUpdate(userId, chargeAmount);
    }

    private UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    private void validateUserPoint(long chargeAmount, UserPoint userPoint) {
        long MAX_POINT = 100_000_000;
        if (userPoint.point() + chargeAmount > MAX_POINT) {
            long availablePoint = MAX_POINT - userPoint.point();
            throw new IllegalArgumentException("남은 충전 한도는 " + availablePoint + " 포인트 입니다.");
        }
    }

    private void validateChargePoint(long chargeAmount) {
        if (chargeAmount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
    }
}