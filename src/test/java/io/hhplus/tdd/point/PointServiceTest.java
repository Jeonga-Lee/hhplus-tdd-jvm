package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PointServiceTest {

    @Autowired
    private UserPointTable userPointTable = new UserPointTable();

    @Autowired
    PointService pointService = new PointService(new UserPointTable());

    //  - PATCH  `/point/{id}/charge` : 포인트를 충전한다.
    @Test
    void success_charge_point() {
        //given
        long userId = 2L;
        long chargeAmount = 100L;
        //when
        pointService.charge(userId, chargeAmount);
        //then
        UserPoint updatedUserPoint = userPointTable.selectById(userId);
        assertThat(updatedUserPoint.point()).isEqualTo(100L);
    }

    @Test
    void fail_no_point_available_to_charge() {
        //given
        long userId = 1L;
        long chargeAmount = 0L;
        //when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, chargeAmount);
        });
        //then
        assertThat(exception.getMessage()).isEqualTo("충전 금액은 0보다 커야 합니다.");
     }

    // mock한 DB의 userPoint()가 Optional.empty()를 주면 함수가 실행되는가?
    @Test
    void fail_when_charge_exceeds_max_balance() {
        //given
        long userId = 1L;
        long chargeAmount = 2_000_000L;

        userPointTable.insertOrUpdate(1L, 99_000_000);

        //when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, chargeAmount);
        });
        //then
        assertThat(exception.getMessage()).isEqualTo("남은 충전 한도는 1000000 포인트 입니다.");
    }

//        - PATCH `/point/{id}/use` : 포인트를 사용한다.
//        - *GET `/point/{id}` : 포인트를 조회한다.*
//        - *GET `/point/{id}/histories` : 포인트 내역을 조회한다.*
//        - *잔고가 부족할 경우, 포인트 사용은 실패하여야 합니다.*
//        - *동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.*
}