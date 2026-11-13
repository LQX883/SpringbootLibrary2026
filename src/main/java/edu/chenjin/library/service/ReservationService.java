package edu.chenjin.library.service;

import edu.chenjin.library.common.BizException;
import edu.chenjin.library.common.PageResult;
import edu.chenjin.library.common.UserContext;
import edu.chenjin.library.dto.ReservationView;
import edu.chenjin.library.entity.Book;
import edu.chenjin.library.entity.Reservation;
import edu.chenjin.library.entity.User;
import edu.chenjin.library.repository.BookRepository;
import edu.chenjin.library.repository.LoanRepository;
import edu.chenjin.library.repository.ReservationRepository;
import edu.chenjin.library.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final NotificationService notificationService;

    public ReservationService(ReservationRepository reservationRepository,
                              BookRepository bookRepository,
                              UserRepository userRepository,
                              LoanRepository loanRepository,
                              NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.notificationService = notificationService;
    }

    public PageResult<ReservationView> myPage(String status, String keyword, int page, int size) {
        return page(UserContext.require().getId(), status, keyword, page, size);
    }

    public PageResult<ReservationView> allPage(String status, String keyword, int page, int size) {
        return page(null, status, keyword, page, size);
    }

    private PageResult<ReservationView> page(Long userId, String status, String keyword, int page, int size) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.min(Math.max(size, 1), 100);
        List<Long> bookIds = StringUtils.hasText(keyword) ? bookRepository.searchIds(keyword.trim()) : List.of();
        Specification<Reservation> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(keyword)) {
                if (bookIds.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("bookId").in(bookIds));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Reservation> result = reservationRepository.findAll(specification,
                PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        Map<Long, Book> books = new HashMap<>();
        Map<Long, User> users = new HashMap<>();
        bookRepository.findAllById(result.getContent().stream().map(Reservation::getBookId).toList())
                .forEach(book -> books.put(book.getId(), book));
        userRepository.findAllById(result.getContent().stream().map(Reservation::getUserId).toList())
                .forEach(user -> users.put(user.getId(), user));

        return PageResult.of(result.getContent().stream()
                .map(item -> toView(item, books.get(item.getBookId()), users.get(item.getUserId())))
                .toList(), result.getTotalElements(), page, pageSize);
    }

    @Transactional
    public ReservationView create(Long bookId) {
        User user = UserContext.require();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BizException(404, "图书不存在"));
        if (!"ACTIVE".equals(book.getStatus())) {
            throw new BizException("该图书已下架，无法预约");
        }
        if (book.getAvailableCopies() != null && book.getAvailableCopies() > 0) {
            throw new BizException("该图书当前有可借副本，请直接借阅");
        }
        if (reservationRepository.existsByUserIdAndBookIdAndStatusIn(
                user.getId(), bookId, List.of("WAITING", "NOTIFIED"))) {
            throw new BizException("您已经预约过该图书，请耐心等待通知");
        }
        if (loanRepository.findFirstByUserIdAndBookIdAndStatus(user.getId(), bookId, "BORROWED").isPresent()) {
            throw new BizException("您正在借阅该图书，无需重复预约");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setBookId(bookId);
        reservation.setStatus("WAITING");
        Reservation saved = reservationRepository.save(reservation);

        long queue = reservationRepository.countByBookIdAndStatus(bookId, "WAITING");
        notificationService.sendToUser(user.getId(), "预约成功",
                "您已成功预约《" + book.getTitle() + "》，当前排队第 " + queue + " 位。有可借副本时会第一时间通知您。",
                "RESERVATION", "/my-reservations");
        return toView(saved, book, user);
    }

    @Transactional
    public void cancel(Long id) {
        User current = UserContext.require();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "预约记录不存在"));
        if (!UserContext.isStaff() && !reservation.getUserId().equals(current.getId())) {
            throw new BizException(403, "只能取消自己的预约");
        }
        if (!List.of("WAITING", "NOTIFIED").contains(reservation.getStatus())) {
            throw new BizException("当前预约状态不允许取消");
        }
        boolean wasNotified = "NOTIFIED".equals(reservation.getStatus());
        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
        if (wasNotified) {
            bookRepository.findById(reservation.getBookId()).ifPresent(this::notifyNextWaiting);
        }
    }

    /**
     * 当图书出现可借副本时，通知队首预约用户。
     */
    @Transactional
    public void notifyNextWaiting(Book book) {
        if (book == null || book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            return;
        }
        Optional<Reservation> waiting = reservationRepository
                .findFirstByBookIdAndStatusOrderByCreatedAtAsc(book.getId(), "WAITING");
        if (waiting.isEmpty()) {
            return;
        }
        Reservation reservation = waiting.get();
        reservation.setStatus("NOTIFIED");
        reservation.setNotifiedAt(LocalDateTime.now());
        reservation.setExpiresAt(LocalDateTime.now().plusHours(48));
        reservationRepository.save(reservation);
        notificationService.sendToUser(reservation.getUserId(), "预约图书已可借阅",
                "您预约的《" + book.getTitle() + "》已有可借副本，请在 48 小时内前往借阅。",
                "RESERVATION", "/books?bookId=" + book.getId());
    }

    private ReservationView toView(Reservation reservation, Book book, User user) {
        return new ReservationView(
                reservation.getId(),
                reservation.getUserId(),
                user == null ? ("用户#" + reservation.getUserId()) : user.getName(),
                reservation.getBookId(),
                book == null ? ("图书#" + reservation.getBookId()) : book.getTitle(),
                book == null ? "" : book.getAuthor(),
                book == null ? null : book.getCoverUrl(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getNotifiedAt(),
                reservation.getExpiresAt()
        );
    }
}
