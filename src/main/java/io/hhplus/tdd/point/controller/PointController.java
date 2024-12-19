package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.LockPointService;
import io.hhplus.tdd.point.service.PointService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/point")
public class PointController {

	private static final Logger log = LoggerFactory.getLogger(PointController.class);
	PointService pointService = new LockPointService(new UserPointTable(), new PointHistoryTable());

	@GetMapping("{id}")
	public UserPoint point(
		@PathVariable long id
	) {
		return pointService.getUserPoint(id);
	}

	@GetMapping("{id}/histories")
	public List<PointHistory> history(
		@PathVariable long id
	) {
		return pointService.selectAllByUserId(id);
	}

	@PatchMapping("{id}/charge")
	public UserPoint charge(
		@PathVariable long id,
		@RequestBody long amount
	) {
		return pointService.charge(id, amount);
	}

	@PatchMapping("{id}/use")
	public UserPoint use(
		@PathVariable long id,
		@RequestBody long amount
	) {
		return pointService.use(id, amount);
	}
}
