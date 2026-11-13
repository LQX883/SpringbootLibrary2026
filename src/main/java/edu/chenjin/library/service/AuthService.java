package edu.chenjin.library.service;

import edu.chenjin.library.common.BizException;
import edu.chenjin.library.common.UserContext;
import edu.chenjin.library.dto.LoginRequest;
import edu.chenjin.library.dto.LoginResponse;
import edu.chenjin.library.dto.RegisterRequest;
import edu.chenjin.library.dto.UserView;
import edu.chenjin.library.entity.User;
import edu.chenjin.library.repository.UserRepository;
import edu.chenjin.library.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AuthService {

    private static final Set<String> REGISTER_ROLES = Set.of("STUDENT", "TEACHER", "OPERATOR");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String name = request.name().trim();
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new BizException(401, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BizException(403, "账号已被停用，请联系管理员");
        }
        user.setLoginTime(LocalDateTime.now());
        userRepository.save(user);
        return new LoginResponse(jwtUtil.generate(user), jwtUtil.getExpireSeconds(), UserView.from(user));
    }

    @Transactional
    public UserView register(RegisterRequest request) {
        String name = request.getName().trim();
        if (!REGISTER_ROLES.contains(request.getRole())) {
            throw new BizException("注册角色只能选择学生、教师或图书管理员");
        }
        if (userRepository.existsByName(name)) {
            throw new BizException("用户名已被注册，请更换一个");
        }
        if (StringUtils.hasText(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
            throw new BizException("该手机号已被注册");
        }

        User user = new User();
        user.setName(name);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setMajor(request.getMajor());
        user.setEmail(request.getEmail());
        user.setStatus("ACTIVE");
        user.setPermissionLevel(request.getPermissionLevel());
        user.setEntryTime(request.getEntryTime());
        user.setDuty(request.getDuty());
        user.setArea(request.getArea());
        user.setGrade(request.getGrade());
        user.setCollege(request.getCollege());
        user.setTitle(request.getTitle());
        return UserView.from(userRepository.save(user));
    }

    public UserView me() {
        User user = userRepository.findById(UserContext.require().getId())
                .orElseThrow(() -> new BizException(401, "账号不存在"));
        return UserView.from(user);
    }
}
