package edu.chenjin.library.dto;

import java.time.LocalDateTime;

public record ReservationView(
        Long id,
        Long userId,
        String userName,
        Long bookId,
        String bookTitle,
        String bookAuthor,
        String bookCover,
        String status,
        LocalDateTime createdAt,
        LocalDateTime notifiedAt,
        LocalDateTime expiresAt
) {
}
