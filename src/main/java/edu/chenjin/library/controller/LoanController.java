package edu.chenjin.library.controller;

import edu.chenjin.library.aspect.LogOperation;
import edu.chenjin.library.common.PageResult;
import edu.chenjin.library.common.Result;
import edu.chenjin.library.dto.BorrowRequest;
import edu.chenjin.library.dto.LoanView;
import edu.chenjin.library.security.RequireRole;
import edu.chenjin.library.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/borrow")
    @LogOperation(module = "借阅", action = "借书", description = "借阅图书")
    public Result<LoanView> borrow(@Valid @RequestBody BorrowRequest request) {
        return Result.ok("借阅成功", loanService.borrow(request.bookId()));
    }

    @PostMapping("/{id}/return")
    @LogOperation(module = "借阅", action = "还书", description = "归还图书")
    public Result<LoanView> returnBook(@PathVariable Long id) {
        return Result.ok("还书成功", loanService.returnBook(id));
    }

    @GetMapping("/my")
    public Result<PageResult<LoanView>> my(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(loanService.myPage(status, keyword, start, end, page, size));
    }

    @GetMapping
    @RequireRole({"ADMIN", "OPERATOR"})
    public Result<PageResult<LoanView>> all(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(loanService.allPage(userId, status, keyword, start, end, page, size));
    }
}
