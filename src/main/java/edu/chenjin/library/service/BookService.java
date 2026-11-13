package edu.chenjin.library.service;

import edu.chenjin.library.common.BizException;
import edu.chenjin.library.common.PageResult;
import edu.chenjin.library.dto.AttachmentView;
import edu.chenjin.library.dto.BookRequest;
import edu.chenjin.library.dto.BookView;
import edu.chenjin.library.entity.Book;
import edu.chenjin.library.entity.BookAttachment;
import edu.chenjin.library.repository.BookAttachmentRepository;
import edu.chenjin.library.repository.BookRepository;
import edu.chenjin.library.repository.LoanRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookAttachmentRepository attachmentRepository;
    private final LoanRepository loanRepository;
    private final FileStorageService fileStorageService;
    private final ReservationService reservationService;

    public BookService(BookRepository bookRepository,
                       BookAttachmentRepository attachmentRepository,
                       LoanRepository loanRepository,
                       FileStorageService fileStorageService,
                       ReservationService reservationService) {
        this.bookRepository = bookRepository;
        this.attachmentRepository = attachmentRepository;
        this.loanRepository = loanRepository;
        this.fileStorageService = fileStorageService;
        this.reservationService = reservationService;
    }

    public PageResult<BookView> page(String keyword, String type, String status, boolean onlyAvailable,
                                     int page, int size) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.min(Math.max(size, 1), 100);
        Specification<Book> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("author")), like),
                        cb.like(cb.lower(root.get("isbn")), like),
                        cb.like(cb.lower(root.get("publisher")), like)
                ));
            }
            if (StringUtils.hasText(type)) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (onlyAvailable) {
                predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Book> result = bookRepository.findAll(specification,
                PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(result.map(book -> BookView.from(book, List.of())));
    }

    public BookView detail(Long id) {
        Book book = find(id);
        List<AttachmentView> attachments = attachmentRepository
                .findByBookIdOrderByImageTypeAscSortOrderAscIdAsc(id)
                .stream().map(AttachmentView::from).toList();
        return BookView.from(book, attachments);
    }

    @Transactional
    public BookView create(BookRequest request) {
        if (StringUtils.hasText(request.getIsbn()) && bookRepository.existsByIsbn(request.getIsbn().trim())) {
            throw new BizException("该 ISBN 已存在，请检查后重试");
        }
        Book book = new Book();
        fill(book, request);
        int total = Math.max(request.getTotalCopies() == null ? 0 : request.getTotalCopies(), 0);
        book.setTotalCopies(total);
        book.setAvailableCopies(total);
        book.setCoverUrl(null);
        Book saved = bookRepository.save(book);
        return BookView.from(saved, List.of());
    }

    @Transactional
    public BookView update(Long id, BookRequest request) {
        Book book = find(id);
        if (StringUtils.hasText(request.getIsbn())
                && bookRepository.existsByIsbnAndIdNot(request.getIsbn().trim(), id)) {
            throw new BizException("该 ISBN 已被其他图书使用");
        }
        long activeLoans = loanRepository.countByBookIdAndStatus(id, "BORROWED");
        int newTotal = request.getTotalCopies() == null ? 0 : request.getTotalCopies();
        if (newTotal < activeLoans) {
            throw new BizException("馆藏数量不能少于当前未归还数量（" + activeLoans + " 本）");
        }
        if ("DISCARDED".equals(request.getStatus()) && activeLoans > 0) {
            throw new BizException("该图书仍有 " + activeLoans + " 本未归还，暂不能下架");
        }
        fill(book, request);
        book.setTotalCopies(newTotal);
        book.setAvailableCopies((int) (newTotal - activeLoans));
        Book saved = bookRepository.save(book);
        if (saved.getAvailableCopies() > 0) {
            reservationService.notifyNextWaiting(saved);
        }
        return detail(saved.getId());
    }

    @Transactional
    public BookView changeStatus(Long id, String status) {
        if (!List.of("ACTIVE", "DISCARDED").contains(status)) {
            throw new BizException("非法的图书状态");
        }
        Book book = find(id);
        if ("DISCARDED".equals(status) && loanRepository.countByBookIdAndStatus(id, "BORROWED") > 0) {
            throw new BizException("仍有未归还副本，不能下架该图书");
        }
        book.setStatus(status);
        return BookView.from(bookRepository.save(book), List.of());
    }

    @Transactional
    public void delete(Long id) {
        Book book = find(id);
        if (loanRepository.countByBookId(id) > 0) {
            throw new BizException("该图书已有借阅记录，为了保留历史数据请使用下架功能");
        }
        List<BookAttachment> attachments = attachmentRepository.findByBookIdOrderByImageTypeAscSortOrderAscIdAsc(id);
        attachments.forEach(item -> fileStorageService.deleteByUrl(item.getUrl()));
        attachmentRepository.deleteByBookId(id);
        bookRepository.delete(book);
    }

    @Transactional
    public BookView uploadCover(Long id, MultipartFile file) {
        Book book = find(id);
        attachmentRepository.findFirstByBookIdAndImageType(id, "COVER").ifPresent(old -> {
            fileStorageService.deleteByUrl(old.getUrl());
            attachmentRepository.delete(old);
        });
        FileStorageService.StoredFile stored = fileStorageService.store(file, "covers");
        BookAttachment attachment = new BookAttachment();
        attachment.setBookId(id);
        attachment.setFileName(stored.fileName());
        attachment.setOriginalName(stored.originalName());
        attachment.setUrl(stored.url());
        attachment.setContentType(stored.contentType());
        attachment.setFileSize(stored.size());
        attachment.setImageType("COVER");
        attachment.setSortOrder(0);
        attachmentRepository.save(attachment);
        book.setCoverUrl(stored.url());
        bookRepository.save(book);
        return detail(id);
    }

    @Transactional
    public BookView uploadGallery(Long id, List<MultipartFile> files) {
        Book book = find(id);
        if (files == null || files.isEmpty()) {
            throw new BizException("请选择要上传的图片");
        }
        long existing = attachmentRepository.countByBookIdAndImageType(id, "GALLERY");
        if (existing + files.size() > 12) {
            throw new BizException("每本图书最多上传 12 张相关图片");
        }
        int index = (int) existing;
        for (MultipartFile file : files) {
            FileStorageService.StoredFile stored = fileStorageService.store(file, "gallery");
            BookAttachment attachment = new BookAttachment();
            attachment.setBookId(id);
            attachment.setFileName(stored.fileName());
            attachment.setOriginalName(stored.originalName());
            attachment.setUrl(stored.url());
            attachment.setContentType(stored.contentType());
            attachment.setFileSize(stored.size());
            attachment.setImageType("GALLERY");
            attachment.setSortOrder(index++);
            attachmentRepository.save(attachment);
        }
        return detail(book.getId());
    }

    @Transactional
    public void deleteAttachment(Long bookId, Long attachmentId) {
        Book book = find(bookId);
        BookAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BizException(404, "图片不存在"));
        if (!attachment.getBookId().equals(bookId)) {
            throw new BizException("该图片不属于当前图书");
        }
        fileStorageService.deleteByUrl(attachment.getUrl());
        attachmentRepository.delete(attachment);
        if ("COVER".equals(attachment.getImageType())) {
            book.setCoverUrl(null);
            bookRepository.save(book);
        }
    }

    public List<String> types() {
        List<String> types = new ArrayList<>();
        for (Object[] row : bookRepository.countBooksByType()) {
            if (row[0] != null) {
                types.add(String.valueOf(row[0]));
            }
        }
        return types;
    }

    public Book find(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "图书不存在"));
    }

    private void fill(Book book, BookRequest request) {
        book.setTitle(request.getTitle().trim());
        book.setAuthor(request.getAuthor().trim());
        book.setType(request.getType());
        book.setPublisher(request.getPublisher());
        book.setIsbn(StringUtils.hasText(request.getIsbn()) ? request.getIsbn().trim() : null);
        book.setDays(request.getDays() == null ? 30 : Math.max(request.getDays(), 1));
        book.setPrice(request.getPrice() == null ? BigDecimal.ZERO : request.getPrice());
        book.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ACTIVE");
        book.setIntroduction(request.getIntroduction());
        book.setShelfNo(request.getShelfNo());
    }
}
