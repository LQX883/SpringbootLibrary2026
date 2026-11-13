package edu.chenjin.library.dto;

import edu.chenjin.library.entity.Book;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookView(
        Long id,
        String title,
        String author,
        String type,
        String publisher,
        String isbn,
        Integer days,
        BigDecimal price,
        Integer totalCopies,
        Integer availableCopies,
        String status,
        String coverUrl,
        String introduction,
        String shelfNo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AttachmentView> attachments
) {

    public static BookView from(Book book, List<AttachmentView> attachments) {
        return new BookView(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getType(),
                book.getPublisher(),
                book.getIsbn(),
                book.getDays(),
                book.getPrice(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getStatus(),
                book.getCoverUrl(),
                book.getIntroduction(),
                book.getShelfNo(),
                book.getCreatedAt(),
                book.getUpdatedAt(),
                attachments == null ? List.of() : attachments
        );
    }
}
