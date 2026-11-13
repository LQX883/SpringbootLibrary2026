package edu.chenjin.library.controller;

import edu.chenjin.library.common.Result;
import edu.chenjin.library.dto.ChartItem;
import edu.chenjin.library.dto.LoanView;
import edu.chenjin.library.dto.StatsOverview;
import edu.chenjin.library.security.RequireRole;
import edu.chenjin.library.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequireRole({"ADMIN", "OPERATOR"})
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public Result<StatsOverview> overview() {
        return Result.ok(statsService.overview());
    }

    @GetMapping("/book-types")
    public Result<List<ChartItem>> bookTypes() {
        return Result.ok(statsService.bookTypeStats());
    }

    @GetMapping("/loan-types")
    public Result<List<ChartItem>> loanTypes() {
        return Result.ok(statsService.loanTypeStats());
    }

    @GetMapping("/monthly")
    public Result<List<ChartItem>> monthly() {
        return Result.ok(statsService.monthlyStats());
    }

    @GetMapping("/top-books")
    public Result<List<ChartItem>> topBooks() {
        return Result.ok(statsService.topBooks());
    }

    @GetMapping("/roles")
    public Result<List<ChartItem>> roles() {
        return Result.ok(statsService.roleStats());
    }

    @GetMapping("/recent-loans")
    public Result<List<LoanView>> recentLoans() {
        return Result.ok(statsService.recentLoans());
    }
}
