package edu.chenjin.library.controller;

import edu.chenjin.library.common.PageResult;
import edu.chenjin.library.common.Result;
import edu.chenjin.library.entity.OperationLog;
import edu.chenjin.library.security.RequireRole;
import edu.chenjin.library.service.OperationLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/logs")
@RequireRole("ADMIN")
public class LogController {

    private final OperationLogService operationLogService;

    public LogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public Result<PageResult<OperationLog>> page(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(operationLogService.page(module, keyword, success, start, end, page, size));
    }
}
