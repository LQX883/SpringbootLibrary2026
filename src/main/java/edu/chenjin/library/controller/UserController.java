package edu.chenjin.library.controller;

import edu.chenjin.library.aspect.LogOperation;
import edu.chenjin.library.common.PageResult;
import edu.chenjin.library.common.Result;
import edu.chenjin.library.dto.StatusRequest;
import edu.chenjin.library.dto.UserView;
import edu.chenjin.library.security.RequireRole;
import edu.chenjin.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequireRole("ADMIN")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<PageResult<UserView>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String major,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userService.page(keyword, role, status, major, page, size));
    }

    @GetMapping("/{id}")
    public Result<UserView> detail(@PathVariable Long id) {
        return Result.ok(userService.get(id));
    }

    @PutMapping("/{id}/status")
    @LogOperation(module = "用户", action = "修改状态", description = "启用或停用用户")
    public Result<UserView> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return Result.ok("用户状态已更新", userService.updateStatus(id, request.status()));
    }
}
