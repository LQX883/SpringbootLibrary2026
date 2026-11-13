package edu.chenjin.library.controller;

import edu.chenjin.library.aspect.LogOperation;
import edu.chenjin.library.common.PageResult;
import edu.chenjin.library.common.Result;
import edu.chenjin.library.dto.ReservationRequest;
import edu.chenjin.library.dto.ReservationView;
import edu.chenjin.library.security.RequireRole;
import edu.chenjin.library.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @LogOperation(module = "预约", action = "预约图书", description = "预约暂无可借副本的图书")
    public Result<ReservationView> create(@Valid @RequestBody ReservationRequest request) {
        return Result.ok("预约成功", reservationService.create(request.bookId()));
    }

    @GetMapping("/my")
    public Result<PageResult<ReservationView>> my(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(reservationService.myPage(status, keyword, page, size));
    }

    @GetMapping
    @RequireRole({"ADMIN", "OPERATOR"})
    public Result<PageResult<ReservationView>> all(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(reservationService.allPage(status, keyword, page, size));
    }

    @DeleteMapping("/{id}")
    @LogOperation(module = "预约", action = "取消预约", description = "取消图书预约")
    public Result<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return Result.ok();
    }
}
