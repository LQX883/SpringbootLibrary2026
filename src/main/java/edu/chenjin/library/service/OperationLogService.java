package edu.chenjin.library.service;

import edu.chenjin.library.common.PageResult;
import edu.chenjin.library.entity.OperationLog;
import edu.chenjin.library.repository.OperationLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    public OperationLogService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    public PageResult<OperationLog> page(String module, String keyword, Boolean success,
                                         LocalDate start, LocalDate end, int page, int size) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.min(Math.max(size, 1), 100);
        Specification<OperationLog> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(module)) {
                predicates.add(cb.equal(root.get("module"), module));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("userName")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("uri")), like)
                ));
            }
            if (success != null) {
                predicates.add(cb.equal(root.get("success"), success));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start.atStartOfDay()));
            }
            if (end != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), end.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<OperationLog> result = operationLogRepository.findAll(specification,
                PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(result);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(OperationLog log) {
        operationLogRepository.save(log);
    }
}
