package edu.chenjin.library.repository;

import edu.chenjin.library.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    long countByStatus(String status);

    long countByBookIdAndStatus(Long bookId, String status);

    boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, List<String> statuses);

    Optional<Reservation> findFirstByBookIdAndStatusOrderByCreatedAtAsc(Long bookId, String status);

    Optional<Reservation> findFirstByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, List<String> statuses);

    long countByUserIdAndStatusIn(Long userId, List<String> statuses);
}
