package edu.chenjin.library.dto;

import edu.chenjin.library.entity.Notification;

import java.time.LocalDateTime;

public record NotificationView(
        Long id,
        String title,
        String content,
        String type,
        Boolean readFlag,
        String link,
        LocalDateTime createdAt
) {

    public static NotificationView from(Notification notification) {
        return new NotificationView(
                notification.getId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getType(),
                notification.getReadFlag(),
                notification.getLink(),
                notification.getCreatedAt()
        );
    }
}
