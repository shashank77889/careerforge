package com.careerforge.recovery;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordRecoveryRepository extends JpaRepository<PasswordRecovery, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from PasswordRecovery r where r.userId = :id")
    Optional<PasswordRecovery> lockByUserId(@org.springframework.data.repository.query.Param("id") Long id);
    Optional<PasswordRecovery> findByTokenHash(String tokenHash);
}
