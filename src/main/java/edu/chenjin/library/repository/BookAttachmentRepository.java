package edu.chenjin.library.repository;

import edu.chenjin.library.entity.BookAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookAttachmentRepository extends JpaRepository<BookAttachment, Long> {

    List<BookAttachment> findByBookIdOrderByImageTypeAscSortOrderAscIdAsc(Long bookId);

    List<BookAttachment> findByBookIdAndImageTypeOrderBySortOrderAscIdAsc(Long bookId, String imageType);

    Optional<BookAttachment> findFirstByBookIdAndImageType(Long bookId, String imageType);

    long countByBookIdAndImageType(Long bookId, String imageType);

    void deleteByBookId(Long bookId);
}
