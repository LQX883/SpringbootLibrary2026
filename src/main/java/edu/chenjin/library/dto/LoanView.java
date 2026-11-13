package edu.chenjin.library.dto;

import java.time.LocalDateTime;

public record LoanView(
        Long id,
        Long userId,
        String userName,
        Long bookId,
        String bookTitle,
        String bookAuthor,
        String bookCover,
        LocalDateTime borrowDate,
        LocalDateTime dueDate,
        LocalDateTime returnDate,
        String status,
        boolean overdue,
        Long daysLeft
) {
}
