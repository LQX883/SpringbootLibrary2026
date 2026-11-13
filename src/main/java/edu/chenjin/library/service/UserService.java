package edu.chenjin.library.service;

import edu.chenjin.library.common.BizException;
import edu.chenjin.library.common.PageResult;
import edu.chenjin.library.common.UserContext;
import edu.chenjin.library.dto.UserView;
import edu.chenjin.library.entity.User;
import edu.chenjin.library.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PageResult<UserView> page(String keyword, String role, String status, String major,
                                     int page, int size) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.min(Math.max(size, 1), 100);
        Specification<User> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("phone")), like),
                        cb.like(cb.lower(root.get("major")), like)
                ));
            }
            if (StringUtils.hasText(role)) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(major)) {
                predicates.add(cb.like(root.get("major"), "%" + major.trim() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<User> result = userRepository.findAll(specification,
                PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(result.map(UserView::from));
    }

    public UserView get(Long id) {
        return UserView.from(find(id));
    }

    @Transactional
    public UserView updateStatus(Long id, String status) {
        User current = UserContext.require();
        User target = find(id);
        if (current.getId().equals(target.getId()) && "DISABLED".equals(status)) {
            throw new BizException("不能停用当前登录账号");
        }
        if (!List.of("ACTIVE", "DISABLED").contains(status)) {
            throw new BizException("非法的用户状态");
        }
        target.setStatus(status);
        return UserView.from(userRepository.save(target));
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "用户不存在"));
    }
}
