package com.wap.wabi.auth.admin.entity;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Value;

@Entity
public class AdminRefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String adminName;
    private String refreshToken;
    private int reissueCount = 0;

    public AdminRefreshToken() {
    }

    public AdminRefreshToken(builder builder) {
        this.adminName = builder.adminName;
        this.refreshToken = builder.refreshToken;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean validateRefreshToken(String refreshToken) {
        return this.refreshToken.equals(refreshToken);
    }

    public void increaseReissueCount() {
        reissueCount++;
    }

    public static class builder {
        private String adminName;
        private String refreshToken;

        public builder adminName(String adminName) {
            this.adminName = adminName;
            return this;
        }

        public builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public AdminRefreshToken build() {
            return new AdminRefreshToken(this);
        }
    }
}
