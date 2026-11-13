package edu.chenjin.library.repository;

import edu.chenjin.library.entity.Loan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    long countByStatus(String status);

    long countByStatusAndDueDateBefore(String status, LocalDateTime time);

    long countByBookIdAndStatus(Long bookId, String status);

    long countByBookId(Long bookId);

    long countByUserIdAndStatus(Long userId, String status);

    Optional<Loan> findFirstByUserIdAndBookIdAndStatus(Long userId, Long bookId, String status);

    List<Loan> findByUserIdOrderByBorrowDateDesc(Long userId);

    @Query("select l.bookId, count(l) from Loan l group by l.bookId order by count(l) desc")
    List<Object[]> findTopBookIds(Pageable pageable);

    @Query("select b.type, count(l) from Loan l, Book b where l.bookId = b.id group by b.type order by count(l) desc")
    List<Object[]> countLoansByBookType();

    @Query(value = "select date_format(borrow_date, '%Y-%m') as m, count(*) as c " +
            "from loan where borrow_date >= :start group by m order by m", nativeQuery = true)
    List<Object[]> countMonthly(@Param("start") LocalDateTime start);

    @Query("select l from Loan l order by l.borrowDate desc")
    List<Loan> findRecent(Pageable pageable);
}
