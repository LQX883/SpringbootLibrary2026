package edu.chenjin.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.chenjin.library.common.Result;
import edu.chenjin.library.common.UserContext;
import edu.chenjin.library.entity.User;
import edu.chenjin.library.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtUtil jwtUtil, UserRepository userRepository, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            writeError(response, 401, "请先登录后再访问");
            return false;
        }

        User user;
        try {
            Claims claims = jwtUtil.parse(token);
            Long userId = Long.valueOf(claims.getSubject());
            user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                writeError(response, 401, "账号不存在或已被删除");
                return false;
            }
            if (!"ACTIVE".equals(user.getStatus())) {
                writeError(response, 403, "账号已被停用，请联系管理员");
                return false;
            }
        } catch (Exception e) {
            writeError(response, 401, "登录已过期，请重新登录");
            return false;
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null) {
            boolean allowed = Arrays.stream(requireRole.value()).anyMatch(role -> role.equalsIgnoreCase(user.getRole()));
            if (!allowed) {
                writeError(response, 403, "当前账号没有执行该操作的权限");
                return false;
            }
        }

        UserContext.set(user);
        request.setAttribute("currentUser", user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header)) {
            return header.startsWith("Bearer ") ? header.substring(7) : header;
        }
        String queryToken = request.getParameter("token");
        return StringUtils.hasText(queryToken) ? queryToken : null;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(status, message)));
    }
}
