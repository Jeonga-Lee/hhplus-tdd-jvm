package io.hhplus.tdd.point.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.LockPointService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LockPointServiceTest {

	@InjectMocks
	LockPointService pointService;
	@Mock
	private UserPointTable userPointTable;
	@Mock
	private PointHistoryTable pointHistoryTable;

	@Test
	void success_charge_point() {
		//given
		long userId = 1L;

		UserPoint userPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());
		when(userPointTable.selectById(userId)).thenReturn(userPoint);

		when(userPointTable.insertOrUpdate(userId, 2000L)).thenReturn(
			new UserPoint(userId, userPoint.point() + 1000L, System.currentTimeMillis()));
		//when
		UserPoint newUserPoint = pointService.charge(userId, 1000L);

		//then
		assertThat(newUserPoint.point()).isEqualTo(2000L);
		verify(userPointTable).insertOrUpdate(userId, userPoint.point() + 1000L);
	}

	@Test
	void fail_charge_zero_point() {
		//given
		long chargeAmount = 0L;

		//when
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			pointService.charge(1L, chargeAmount);
		});

		//then
		assertThat(exception.getMessage()).isEqualTo("최소 100 포인트 이상 입력해주세요.");
		verify(userPointTable, never()).insertOrUpdate(1L, 0L);
	}

	@Test
	void fail_charge_point_exceeds_limit() {
		//given
		long userId = 1L;

		UserPoint userPoint = new UserPoint(userId, 900_000L, System.currentTimeMillis());
		when(userPointTable.selectById(userId)).thenReturn(userPoint);

		//when
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			pointService.charge(userId, 200_000L);
		});

		//then
		assertThat(exception.getMessage()).isEqualTo("남은 충전 한도는 100000 포인트 입니다.");
		verify(userPointTable, never()).insertOrUpdate(userId, userPoint.point() + 200_000L);
	}

	@Test
	void success_use_point() {
		//given
		long userId = 1L;

		UserPoint userPoint = new UserPoint(userId, 2000L, System.currentTimeMillis());
		when(userPointTable.selectById(userId)).thenReturn(userPoint);

		when(userPointTable.insertOrUpdate(userId, 1000L)).thenReturn(
			new UserPoint(userId, userPoint.point() - 1000L, System.currentTimeMillis()));
		//when
		UserPoint newUserPoint = pointService.use(userId, 1000L);

		//then
		assertThat(newUserPoint.point()).isEqualTo(1000L);
		verify(userPointTable).insertOrUpdate(userId, userPoint.point() - 1000L);
	}

	@Test
	void fail_use_zero_point() {
		//given
		long amount = 0L;

		//when
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			pointService.use(1L, amount);
		});

		//then
		assertThat(exception.getMessage()).isEqualTo("최소 100 포인트 이상 입력해주세요.");
		verify(userPointTable, never()).insertOrUpdate(1L, 0L);
	}

	@Test
	void fail_use_point_insufficient() {
		//given
		long userId = 1L;

		UserPoint userPoint = new UserPoint(userId, 100_000L, System.currentTimeMillis());
		when(userPointTable.selectById(userId)).thenReturn(userPoint);

		//when
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			pointService.use(userId, 200_000L);
		});

		//then
		assertThat(exception.getMessage()).isEqualTo(
			"포인트가 부족합니다. 현재 " + 100_000L + "포인트 보유 중 입니다.");
		verify(userPointTable, never()).insertOrUpdate(userId, userPoint.point() - 200_000L);
	}

	@Test
	void fail_use_point_improper() {
		//given
		long amount = 99L;

		//when
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			pointService.use(1L, amount);
		});

		//then
		assertThat(exception.getMessage()).isEqualTo("100 포인트 단위로 충전 및 사용이 가능합니다.");
		verify(userPointTable, never()).insertOrUpdate(1L, 99L);
	}

	@Test
	void success_check_point() {
		//given
		UserPoint userPoint = new UserPoint(1L, 2000L, System.currentTimeMillis());
		when(userPointTable.selectById(1L)).thenReturn(userPoint);

		//when
		UserPoint savedUserPoint = pointService.getUserPoint(1L);

		//then
		assertThat(savedUserPoint.point()).isEqualTo(2000L);
	}


}
