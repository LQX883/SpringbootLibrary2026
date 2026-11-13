package edu.chenjin.library.controller;

import edu.chenjin.library.aspect.LogOperation;
import edu.chenjin.library.common.Result;
import edu.chenjin.library.common.UserContext;
import edu.chenjin.library.dto.LoginRequest;
import edu.chenjin.library.dto.LoginResponse;
import edu.chenjin.library.dto.RegisterRequest;
import edu.chenjin.library.dto.UserView;
import edu.chenjin.library.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @LogOperation(module = "认证", action = "登录", description = "用户登录系统", saveParams = false)
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        LoginResponse response = authService.login(request);
        servletRequest.setAttribute("logUserName", request.name());
        return Result.ok("登录成功", response);
    }

    @PostMapping("/register")
    @LogOperation(module = "认证", action = "注册", description = "新用户注册", saveParams = false)
    public Result<UserView> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok("注册成功", authService.register(request));
    }

    @GetMapping("/me")
    public Result<UserView> me() {
        return Result.ok(authService.me());
    }

    @PostMapping("/logout")
    @LogOperation(module = "认证", action = "退出", description = "用户退出登录")
    public Result<Void> logout() {
        UserContext.clear();
        return Result.ok();
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        return Result.ok(Map.of("status", "UP", "service", "SpringbootLibrary2026"));
    }
}
