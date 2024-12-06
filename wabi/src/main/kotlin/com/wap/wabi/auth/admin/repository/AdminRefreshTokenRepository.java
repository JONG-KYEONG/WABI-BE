package com.wap.wabi.auth.admin.repository;

import com.wap.wabi.auth.admin.entity.AdminRefreshToken;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AdminRefreshTokenRepository extends JpaRepository<AdminRefreshToken, Long> {
    Optional<AdminRefreshToken> findAdminRefreshTokenByAdminNameAndReissueCountLessThan(String name, long count);
    Optional<AdminRefreshToken> findAdminRefreshTokenByAdminName(String name);
}
