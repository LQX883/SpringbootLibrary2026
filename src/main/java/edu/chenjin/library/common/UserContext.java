package edu.chenjin.library.common;

import edu.chenjin.library.entity.User;

public final class UserContext {

    private static final ThreadLocal<User> CURRENT = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(User user) {
        CURRENT.set(user);
    }

    public static User get() {
        return CURRENT.get();
    }

    public static User require() {
        User user = get();
        if (user == null) {
            throw new BizException(401, "登录状态已失效，请重新登录");
        }
        return user;
    }

    public static Long id() {
        User user = get();
        return user == null ? null : user.getId();
    }

    public static String name() {
        User user = get();
        return user == null ? null : user.getName();
    }

    public static String role() {
        User user = get();
        return user == null ? null : user.getRole();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(role());
    }

    public static boolean isStaff() {
        return isAdmin() || "OPERATOR".equals(role());
    }

    public static void clear() {
        CURRENT.remove();
    }
}
