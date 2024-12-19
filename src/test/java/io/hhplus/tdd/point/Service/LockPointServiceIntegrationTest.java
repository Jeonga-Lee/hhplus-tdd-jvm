package io.hhplus.tdd.point.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.service.LockPointService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LockPointServiceIntegrationTest {

	@Autowired
	private LockPointService pointService;

	@Autowired
	private UserPointTable userPointTable;

	@Autowired
	private PointHistoryTable pointHistoryTable;

	@Test
	void success_leave_history_when_charge_point() {
		// given
		long userId = 1L;
		long initialAmount = 0L;
		long chargeAmount = 1000L;

		userPointTable.insertOrUpdate(userId, initialAmount);

		// when
		pointService.charge(userId, chargeAmount);
		pointService.charge(userId, chargeAmount);

		// then
		List<PointHistory> history = pointHistoryTable.selectAllByUserId(userId);
		assertThat(history).hasSize(2);
		assertThat(history.get(1).type()).isEqualTo(TransactionType.CHARGE);
		assertThat(history.get(1).amount()).isEqualTo(chargeAmount);
	}

	@Test
	void success_leave_history_when_use_point() {
		// given
		long userId = 2L;
		long initialAmount = 2000L;
		long useAmount = 1000L;

		userPointTable.insertOrUpdate(userId, initialAmount);

		// when
		pointService.use(userId, useAmount);

		// then
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

		userPointTable.insertOrUpdate(userId, initialAmount);

		// when & then
		assertThatThrownBy(() -> pointService.use(userId, useAmount))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(
				"포인트가 부족합니다. 현재 " + initialAmount + "포인트 보유 중 입니다.");
	}


	@DisplayName("같은 유저에 대해 포인트 충전과 사용이 순차적으로 수행된다.")
	@Test
	void success_singleUser_syncProcessing() {
		// given
		long userId = 5L;
		long amount = 1000L;

		// when
		ExecutorService executorService = Executors.newFixedThreadPool(10);

		for (int i = 0; i < 1000; i++) {
			executorService.submit(() -> {
				pointService.charge(userId, amount);
				pointService.use(userId, amount);
			});
		}

		// then
		executorService.shutdown();
		UserPoint finalUserPoint = userPointTable.selectById(userId);
		assertThat(finalUserPoint.point()).isEqualTo(0L);
	}
}
