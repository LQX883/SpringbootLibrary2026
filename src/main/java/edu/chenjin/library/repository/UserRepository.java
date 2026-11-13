package edu.chenjin.library.repository;

import edu.chenjin.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByName(String name);

    boolean existsByName(String name);

    boolean existsByPhone(String phone);

    long countByRole(String role);

    long countByStatus(String status);

    @Query("select u.id from User u where lower(u.name) like lower(concat('%', :keyword, '%')) " +
            "or lower(coalesce(u.phone, '')) like lower(concat('%', :keyword, '%'))")
    List<Long> searchIds(@Param("keyword") String keyword);
}
