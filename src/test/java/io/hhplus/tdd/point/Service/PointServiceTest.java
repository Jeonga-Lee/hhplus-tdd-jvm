package io.hhplus.tdd.point.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.service.PointService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PointServiceTest {

	@Autowired
	private PointService pointService;

	@Autowired
	private UserPointTable userPointTable;

	@Autowired
	private PointHistoryTable pointHistoryTable;

	@Test
	void success_charge_point() {
		// given
		long userId = 1L;
		long initialAmount = 0L;
		long chargeAmount = 1000L;

		userPointTable.insertOrUpdate(userId, initialAmount);

		// when
		UserPoint updatedUserPoint = pointService.charge(userId, chargeAmount);

		// then
		assertThat(updatedUserPoint.point()).isEqualTo(1000L);

		List<PointHistory> history = pointHistoryTable.selectAllByUserId(userId);
		assertThat(history).hasSize(1);
		assertThat(history.get(0).type()).isEqualTo(TransactionType.CHARGE);
		assertThat(history.get(0).amount()).isEqualTo(chargeAmount);
	}

	@Test
	void success_use_point() {
		// given
		long userId = 2L;
		long initialAmount = 2000L;
		long useAmount = 1000L;

		userPointTable.insertOrUpdate(userId, initialAmount);

		// when
		UserPoint updatedUserPoint = pointService.use(userId, useAmount);

		// then
		assertThat(updatedUserPoint.point()).isEqualTo(1000L);

		List<PointHistory> history = pointHistoryTable.selectAllByUserId(userId);
		assertThat(history).hasSize(1);
		assertThat(history.get(0).type()).isEqualTo(TransactionType.USE);
		assertThat(history.get(0).amount()).isEqualTo(useAmount);
	}

	@Test
	void fail_charge_point_exceeds_limit() {
		// given
		long userId = 3L;
		long initialAmount = 999_000L;
		long chargeAmount = 2000L;

		// 초기 데이터 설정 (UserPoint)
		userPointTable.insertOrUpdate(userId, initialAmount);

		// when & then
		assertThatThrownBy(() -> pointService.charge(userId, chargeAmount))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("남은 충전 한도는 1000 포인트 입니다.");
	}

	@Test
	void fail_use_point_insufficient() {
		// given
		long userId = 4L;
		long initialAmount = 500L;
		long useAmount = 1000L;

		// 초기 데이터 설정 (UserPoint)
		userPointTable.insertOrUpdate(userId, initialAmount);

		// when & then
		assertThatThrownBy(() -> pointService.use(userId, useAmount))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(
				"포인트가 부족합니다." + initialAmount + "포인트 사용 가능합니다.");
	}
}
