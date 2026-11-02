package com.careerforge.recovery;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "password_recovery")
public class PasswordRecovery {
    @Id public Long userId;
    @Column(length = 64, unique = true) public String tokenHash;
    public Instant expiresAt;
    public Instant requestedAt;
    @Column(nullable = false) public long sessionVersion;
}
