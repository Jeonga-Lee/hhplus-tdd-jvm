package io.hhplus.tdd.point.domain;

import io.hhplus.tdd.point.UserPoint;

public class UserPointDomain {

	private final UserPoint userPoint;

	public UserPointDomain(UserPoint userPoint) {
		this.userPoint = userPoint;
	}

	public UserPoint charge(long chargeAmount) {
		validatePoint(chargeAmount);
		validateMaxPoint(chargeAmount);

		return new UserPoint(userPoint.id(), userPoint.point() + chargeAmount,
			System.currentTimeMillis());
	}

	public UserPoint use(long amount) {
		validatePoint(amount);
		validateExistPoint(amount);

		return new UserPoint(userPoint.id(), userPoint.point() - amount,
			System.currentTimeMillis());
	}

	private void validateExistPoint(long amount) {
		if (userPoint.point() - amount < 0) {
			throw new IllegalArgumentException(
				"포인트가 부족합니다." + userPoint.point() + "포인트 사용 가능합니다.");
		}
	}

	private void validateMaxPoint(long chargeAmount) {
		long MAX_POINT = 1_000_000;
		if (userPoint.point() + chargeAmount > MAX_POINT) {
			long availablePoint = MAX_POINT - userPoint.point();
			throw new IllegalArgumentException("남은 충전 한도는 " + availablePoint + " 포인트 입니다.");
		}
	}

	private void validatePoint(long chargeAmount) {
		if (chargeAmount <= 0) {
			throw new IllegalArgumentException("100 포인트 이상 입력해주세요.");
		}
		if (chargeAmount % 100 > 0) {
			throw new IllegalArgumentException("100 포인트 단위로 충전 및 사용이 가능합니다.");
		}
	}
}
