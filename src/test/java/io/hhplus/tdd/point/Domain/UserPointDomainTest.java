package io.hhplus.tdd.point.Domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.domain.UserPointDomain;
import org.junit.jupiter.api.Test;

class UserPointDomainTest {

	//  - PATCH  `/point/{id}/charge` : 포인트를 충전한다.
	@Test
	void success_charge_point() {
		//given
		UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());
		UserPointDomain userPointDomain = new UserPointDomain(userPoint);

		//when
		UserPoint newUserPoint = userPointDomain.charge(1000L);

		//then
		assertThat(newUserPoint.point()).isEqualTo(2000L);
	}

	@Test
	void fail_no_point_to_charge() {
		//given
		UserPoint userPoint = new UserPoint(1L, 0L, System.currentTimeMillis());
		UserPointDomain userPointDomain = new UserPointDomain(userPoint);

		//when
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userPointDomain.charge(0L);
		});

		//then
		assertThat(exception.getMessage()).isEqualTo("100 포인트 이상 입력해주세요.");
	}

	@Test
	void fail_charge_exceeds_max_balance() {
		//given
		UserPoint userPoint = new UserPoint(1L, 900_000L, System.currentTimeMillis());
		UserPointDomain userPointDomain = new UserPointDomain(userPoint);

		//when
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userPointDomain.charge(200_000L);
		});

		//then
		assertThat(exception.getMessage()).isEqualTo("남은 충전 한도는 100000 포인트 입니다.");
	}

	// - PATCH `/point/{id}/use` : 포인트를 사용한다.
	@Test
	void success_use_point() {
		//given
		UserPoint userPoint = new UserPoint(1L, 300_000L, System.currentTimeMillis());
		UserPointDomain userPointDomain = new UserPointDomain(userPoint);

		//when
		UserPoint newUserPoint = userPointDomain.use(100_000L);

		//then
		assertThat(newUserPoint.point()).isEqualTo(200_000L);
	}

	@Test
	void fail_no_point_to_use() {
		//given
		UserPoint userPoint = new UserPoint(1L, 300_000L, System.currentTimeMillis());
		UserPointDomain userPointDomain = new UserPointDomain(userPoint);

		//when
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userPointDomain.use(0L);
		});

		//then
		assertThat(exception.getMessage()).isEqualTo("100 포인트 이상 입력해주세요.");
	}

	@Test
	void fail_not_enough_balance_to_use() {
		//given
		UserPoint userPoint = new UserPoint(1L, 100_000L, System.currentTimeMillis());
		UserPointDomain userPointDomain = new UserPointDomain(userPoint);

		//when
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userPointDomain.use(200_000L);
		});

		//then
		assertThat(exception.getMessage()).isEqualTo(
			"포인트가 부족합니다." + userPoint.point() + "포인트 사용 가능합니다.");
	}

	@Test
	void fail_improper_point_to_use() {
		//given
		UserPoint userPoint = new UserPoint(1L, 100_000L, System.currentTimeMillis());
		UserPointDomain userPointDomain = new UserPointDomain(userPoint);

		//when
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userPointDomain.use(990L);
		});

		//then
		assertThat(exception.getMessage()).isEqualTo("100 포인트 단위로 충전 및 사용이 가능합니다.");
	}

	// - *GET `/point/{id}` : 포인트를 조회한다.*
	@Test
	void success_check_point() {
		//given
		UserPoint userPoint = new UserPoint(1L, 100_000L, System.currentTimeMillis());

		//then
		assertThat(userPoint.point()).isEqualTo(100_000L);
	}


}
