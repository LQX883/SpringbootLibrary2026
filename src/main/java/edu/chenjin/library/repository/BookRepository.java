package edu.chenjin.library.repository;

import edu.chenjin.library.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    long countByStatus(String status);

    @Query("select coalesce(sum(b.totalCopies), 0) from Book b")
    long sumTotalCopies();

    @Query("select coalesce(sum(b.availableCopies), 0) from Book b")
    long sumAvailableCopies();

    @Query("select b.type, count(b) from Book b group by b.type order by count(b) desc")
    List<Object[]> countBooksByType();

    @Query("select b.id from Book b where lower(b.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(b.author) like lower(concat('%', :keyword, '%')) " +
            "or lower(coalesce(b.isbn, '')) like lower(concat('%', :keyword, '%'))")
    List<Long> searchIds(@Param("keyword") String keyword);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);
}
