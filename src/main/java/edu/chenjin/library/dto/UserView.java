package edu.chenjin.library.dto;

import edu.chenjin.library.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserView(
        Long id,
        String name,
        String phone,
        String role,
        String major,
        String status,
        String email,
        LocalDateTime loginTime,
        Integer permissionLevel,
        LocalDate entryTime,
        String duty,
        String area,
        String grade,
        String college,
        String title,
        LocalDateTime createdAt
) {

    public static UserView from(User user) {
        return new UserView(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getMajor(),
                user.getStatus(),
                user.getEmail(),
                user.getLoginTime(),
                user.getPermissionLevel(),
                user.getEntryTime(),
                user.getDuty(),
                user.getArea(),
                user.getGrade(),
                user.getCollege(),
                user.getTitle(),
                user.getCreatedAt()
        );
    }
}
