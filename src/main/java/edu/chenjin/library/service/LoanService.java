package edu.chenjin.library.service;

import edu.chenjin.library.common.BizException;
import edu.chenjin.library.common.PageResult;
import edu.chenjin.library.common.UserContext;
import edu.chenjin.library.dto.LoanView;
import edu.chenjin.library.entity.Book;
import edu.chenjin.library.entity.Loan;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final NotificationService notificationService;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       UserRepository userRepository,
                       ReservationRepository reservationRepository,
                       ReservationService reservationService,
                       NotificationService notificationService) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
    }

    public PageResult<LoanView> myPage(String status, String keyword, LocalDate start, LocalDate end,
                                       int page, int size) {
        return page(UserContext.require().getId(), status, keyword, start, end, page, size);
    }

    public PageResult<LoanView> allPage(Long userId, String status, String keyword, LocalDate start, LocalDate end,
                                        int page, int size) {
        return page(userId, status, keyword, start, end, page, size);
    }

    private PageResult<LoanView> page(Long userId, String status, String keyword,
                                      LocalDate start, LocalDate end, int page, int size) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.min(Math.max(size, 1), 100);
        List<Long> bookIds = StringUtils.hasText(keyword) ? bookRepository.searchIds(keyword.trim()) : List.of();
        List<Long> userIds = StringUtils.hasText(keyword) ? userRepository.searchIds(keyword.trim()) : List.of();

        Specification<Loan> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (StringUtils.hasText(status)) {
                if ("OVERDUE".equalsIgnoreCase(status)) {
                    predicates.add(cb.equal(root.get("status"), "BORROWED"));
                    predicates.add(cb.lessThan(root.get("dueDate"), LocalDateTime.now()));
                } else {
                    predicates.add(cb.equal(root.get("status"), status));
                }
            }
            if (StringUtils.hasText(keyword)) {
                if (bookIds.isEmpty() && userIds.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    List<Predicate> keywordPredicates = new ArrayList<>();
                    if (!bookIds.isEmpty()) {
                        keywordPredicates.add(root.get("bookId").in(bookIds));
                    }
                    if (!userIds.isEmpty()) {
                        keywordPredicates.add(root.get("userId").in(userIds));
                    }
                    predicates.add(cb.or(keywordPredicates.toArray(new Predicate[0])));
                }
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("borrowDate"), start.atStartOfDay()));
            }
            if (end != null) {
                predicates.add(cb.lessThan(root.get("borrowDate"), end.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Loan> result = loanRepository.findAll(specification,
                PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "borrowDate")));
        Map<Long, Book> books = new HashMap<>();
        Map<Long, User> users = new HashMap<>();
        bookRepository.findAllById(result.getContent().stream().map(Loan::getBookId).toList())
                .forEach(book -> books.put(book.getId(), book));
        userRepository.findAllById(result.getContent().stream().map(Loan::getUserId).toList())
                .forEach(user -> users.put(user.getId(), user));
        return PageResult.of(result.getContent().stream()
                .map(loan -> toView(loan, books.get(loan.getBookId()), users.get(loan.getUserId())))
                .toList(), result.getTotalElements(), page, pageSize);
    }

    @Transactional
    public LoanView borrow(Long bookId) {
        User user = UserContext.require();
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new BizException(404, "图书不存在"));
        if (!"ACTIVE".equals(book.getStatus())) {
            throw new BizException("该图书已下架，无法借阅");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new BizException("当前暂无可借副本，您可以选择预约该图书");
        }
        if (loanRepository.findFirstByUserIdAndBookIdAndStatus(user.getId(), bookId, "BORROWED").isPresent()) {
            throw new BizException("您已借阅该图书且尚未归还");
        }

        LocalDateTime now = LocalDateTime.now();
        int days = book.getDays() == null ? 30 : book.getDays();
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Loan loan = new Loan();
        loan.setUserId(user.getId());
        loan.setBookId(bookId);
        loan.setBorrowDate(now);
        loan.setDueDate(now.plusDays(days));
        loan.setStatus("BORROWED");
        loan.setOperatorId(user.getId());
        Loan saved = loanRepository.save(loan);

        reservationRepository.findFirstByUserIdAndBookIdAndStatusIn(
                        user.getId(), bookId, List.of("WAITING", "NOTIFIED"))
                .ifPresent(reservation -> {
                    reservation.setStatus("FULFILLED");
                    reservationRepository.save(reservation);
                });

        notificationService.sendToUser(user.getId(), "借阅成功",
                "您已成功借阅《" + book.getTitle() + "》，请于 " + loan.getDueDate().toLocalDate() + " 前归还。",
                "LOAN", "/my-loans");
        return toView(saved, book, user);
    }

    @Transactional
    public LoanView returnBook(Long loanId) {
        User current = UserContext.require();
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BizException(404, "借阅记录不存在"));
        if (!"BORROWED".equals(loan.getStatus())) {
            throw new BizException("该借阅记录已归还");
        }
        if (!UserContext.isStaff() && !loan.getUserId().equals(current.getId())) {
            throw new BizException(403, "只能归还自己的借阅记录");
        }

        loan.setStatus("RETURNED");
        loan.setReturnDate(LocalDateTime.now());
        loanRepository.save(loan);

        Book book = bookRepository.findByIdForUpdate(loan.getBookId()).orElse(null);
        if (book != null) {
            int available = book.getAvailableCopies() == null ? 0 : book.getAvailableCopies();
            int total = book.getTotalCopies() == null ? available + 1 : book.getTotalCopies();
            book.setAvailableCopies(Math.min(total, available + 1));
            bookRepository.save(book);
            reservationService.notifyNextWaiting(book);
        }

        User borrower = userRepository.findById(loan.getUserId()).orElse(null);
        notificationService.sendToUser(loan.getUserId(), "还书成功",
                "《" + (book == null ? "图书" : book.getTitle()) + "》已归还，感谢您爱护馆藏。",
                "LOAN", "/my-loans");
        return toView(loan, book, borrower);
    }

    public List<LoanView> recent(int limit) {
        PageRequest pageable = PageRequest.of(0, Math.max(limit, 1));
        List<Loan> loans = loanRepository.findRecent(pageable);
        Map<Long, Book> books = new HashMap<>();
        Map<Long, User> users = new HashMap<>();
        bookRepository.findAllById(loans.stream().map(Loan::getBookId).toList())
                .forEach(book -> books.put(book.getId(), book));
        userRepository.findAllById(loans.stream().map(Loan::getUserId).toList())
                .forEach(user -> users.put(user.getId(), user));
        return loans.stream()
                .map(loan -> toView(loan, books.get(loan.getBookId()), users.get(loan.getUserId())))
                .toList();
    }

    private LoanView toView(Loan loan, Book book, User user) {
        boolean overdue = "BORROWED".equals(loan.getStatus())
                && loan.getDueDate() != null
                && loan.getDueDate().isBefore(LocalDateTime.now());
        Long daysLeft = null;
        if ("BORROWED".equals(loan.getStatus()) && loan.getDueDate() != null) {
            daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), loan.getDueDate().toLocalDate());
        }
        return new LoanView(
                loan.getId(),
                loan.getUserId(),
                user == null ? ("用户#" + loan.getUserId()) : user.getName(),
                loan.getBookId(),
                book == null ? ("图书#" + loan.getBookId()) : book.getTitle(),
                book == null ? "" : book.getAuthor(),
                book == null ? null : book.getCoverUrl(),
                loan.getBorrowDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                overdue ? "OVERDUE" : loan.getStatus(),
                overdue,
                daysLeft
        );
    }
}
