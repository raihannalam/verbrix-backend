package com.verbrix.repository;

import com.verbrix.model.rbac.RefreshToken;
import com.verbrix.model.rbac.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(Long userId);

    // --- NEW: Deletes the specific token tied to a device ---
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.device.id = :deviceId")
    void deleteByDeviceId(Long deviceId);
}