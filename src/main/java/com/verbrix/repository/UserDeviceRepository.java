package com.verbrix.repository;

import com.verbrix.model.security.UserDevice;
import com.verbrix.model.rbac.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    // Looks up the specific device by its UUID for the current user
    Optional<UserDevice> findByUserAndDeviceIdentifier(User user, String deviceIdentifier);

    List<UserDevice> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserDevice ud WHERE ud.user.id = :userId")
    void deleteAllByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserDevice ud WHERE ud.id = :deviceId AND ud.user.id = :userId")
    void deleteByDeviceIdAndUserId(Long deviceId, Long userId);
}