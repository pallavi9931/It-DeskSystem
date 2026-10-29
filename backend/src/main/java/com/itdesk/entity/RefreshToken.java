package com.itdesk.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class RefreshToken extends BaseEntity {
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="user_id",nullable=false)

    private User user;
    @Column(name="token",nullable=false,unique=true,length=500)
    private String token;
    @Column(name="expiry_date",nullable=false)
    private Instant expiryDate;
    @Builder.Default
    @Column(name="is_revoked",nullable=false)
    private boolean revoked=false;

    public boolean isExpired()
    {
        return Instant.now().isAfter(this.expiryDate);
    }



}
