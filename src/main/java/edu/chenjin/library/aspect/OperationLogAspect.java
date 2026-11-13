package edu.chenjin.library.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.chenjin.library.common.UserContext;
import edu.chenjin.library.entity.OperationLog;
import edu.chenjin.library.entity.User;
import edu.chenjin.library.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    public OperationLogAspect(OperationLogService operationLogService, ObjectMapper objectMapper) {
        this.operationLogService = operationLogService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            save(joinPoint, logOperation, true, null, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable throwable) {
            save(joinPoint, logOperation, false, throwable.getMessage(), System.currentTimeMillis() - start);
            throw throwable;
        }
    }

    private void save(ProceedingJoinPoint joinPoint, LogOperation annotation,
                      boolean success, String error, long duration) {
        try {
            HttpServletRequest request = currentRequest();
            OperationLog operationLog = new OperationLog();
            User user = UserContext.get();
            if (user != null) {
                operationLog.setUserId(user.getId());
                operationLog.setUserName(user.getName());
            } else if (request != null && request.getAttribute("logUserName") != null) {
                operationLog.setUserName(String.valueOf(request.getAttribute("logUserName")));
            }
            operationLog.setModule(annotation.module());
            operationLog.setAction(annotation.action());
            operationLog.setDescription(StringUtils.hasText(annotation.description())
                    ? annotation.description()
                    : ((MethodSignature) joinPoint.getSignature()).getMethod().getName());
            operationLog.setSuccess(success);
            operationLog.setErrorMsg(truncate(error, 500));
            operationLog.setDurationMs(duration);
            if (request != null) {
                operationLog.setMethod(request.getMethod());
                operationLog.setUri(truncate(request.getRequestURI(), 255));
                operationLog.setIp(clientIp(request));
            }
            if (annotation.saveParams()) {
                operationLog.setParams(truncate(serializeArgs(joinPoint.getArgs()), 2000));
            }
            operationLogService.write(operationLog);
        } catch (Exception e) {
            log.warn("操作日志记录失败：{}", e.getMessage());
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String serializeArgs(Object[] args) {
        List<Object> safeArgs = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof MultipartFile || arg instanceof HttpServletRequest) {
                safeArgs.add("[file]");
            } else if (arg != null && arg.getClass().getName().toLowerCase().contains("password")) {
                safeArgs.add("{}");
            } else {
                safeArgs.add(arg);
            }
        }
        try {
            return objectMapper.writeValueAsString(safeArgs);
        } catch (Exception e) {
            return "[参数无法序列化]";
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
