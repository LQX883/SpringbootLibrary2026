package edu.chenjin.library.service;

import edu.chenjin.library.dto.ChartItem;
import edu.chenjin.library.dto.LoanView;
import edu.chenjin.library.dto.StatsOverview;
import edu.chenjin.library.entity.Book;
import edu.chenjin.library.repository.BookRepository;
import edu.chenjin.library.repository.LoanRepository;
import edu.chenjin.library.repository.ReservationRepository;
import edu.chenjin.library.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final LoanService loanService;

    public StatsService(BookRepository bookRepository,
                        UserRepository userRepository,
                        LoanRepository loanRepository,
                        ReservationRepository reservationRepository,
                        LoanService loanService) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
        this.loanService = loanService;
    }

    @Transactional(readOnly = true)
    public StatsOverview overview() {
        return new StatsOverview(
                bookRepository.count(),
                bookRepository.sumTotalCopies(),
                bookRepository.sumAvailableCopies(),
                userRepository.count(),
                loanRepository.count(),
                loanRepository.countByStatus("BORROWED"),
                loanRepository.countByStatusAndDueDateBefore("BORROWED", LocalDateTime.now()),
                reservationRepository.countByStatus("WAITING")
        );
    }

    @Transactional(readOnly = true)
    public List<ChartItem> bookTypeStats() {
        return toChart(bookRepository.countBooksByType(), "未分类");
    }

    @Transactional(readOnly = true)
    public List<ChartItem> loanTypeStats() {
        return toChart(loanRepository.countLoansByBookType(), "未分类");
    }

    @Transactional(readOnly = true)
    public List<ChartItem> monthlyStats() {
        YearMonth now = YearMonth.now();
        YearMonth start = now.minusMonths(5);
        List<Object[]> rows = loanRepository.countMonthly(start.atDay(1).atStartOfDay());
        Map<String, Long> values = new HashMap<>();
        for (Object[] row : rows) {
            values.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        List<ChartItem> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 0; i < 6; i++) {
            YearMonth month = start.plusMonths(i);
            String key = month.format(formatter);
            result.add(new ChartItem(month.getMonthValue() + "月", values.getOrDefault(key, 0L)));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ChartItem> topBooks() {
        List<Object[]> rows = loanRepository.findTopBookIds(PageRequest.of(0, 6));
        Map<Long, Book> books = new HashMap<>();
        bookRepository.findAllById(rows.stream().map(row -> ((Number) row[0]).longValue()).toList())
                .forEach(book -> books.put(book.getId(), book));
        List<ChartItem> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long bookId = ((Number) row[0]).longValue();
            Book book = books.get(bookId);
            result.add(new ChartItem(book == null ? ("图书#" + bookId) : book.getTitle(),
                    ((Number) row[1]).longValue()));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ChartItem> roleStats() {
        List<ChartItem> result = new ArrayList<>();
        result.add(new ChartItem("管理员", userRepository.countByRole("ADMIN")));
        result.add(new ChartItem("图书管理员", userRepository.countByRole("OPERATOR")));
        result.add(new ChartItem("学生", userRepository.countByRole("STUDENT")));
        result.add(new ChartItem("教师", userRepository.countByRole("TEACHER")));
        return result;
    }

    public List<LoanView> recentLoans() {
        return loanService.recent(6);
    }

    private List<ChartItem> toChart(List<Object[]> rows, String emptyName) {
        List<ChartItem> result = new ArrayList<>();
        for (Object[] row : rows) {
            String name = row[0] == null ? emptyName : String.valueOf(row[0]);
            result.add(new ChartItem(name, ((Number) row[1]).longValue()));
        }
        return result;
    }
}
