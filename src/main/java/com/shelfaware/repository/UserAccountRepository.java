package com.shelfaware.repository;

import com.shelfaware.domain.UserAccount;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    @Modifying
    @Query("delete from UserAccount user where user.username like concat(:prefix, '%') and user.createdAt < :cutoff")
    int deleteStaleDemoUsers(@Param("prefix") String prefix, @Param("cutoff") LocalDateTime cutoff);
}
