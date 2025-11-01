package com.energy.device.repository;

import com.energy.device.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    List<UserDevice> findByUserId(Long userId);

    boolean existsByUserIdAndDeviceId(Long userId, Long deviceId);

    Optional<UserDevice> findByUserIdAndDeviceId(Long userId, Long deviceId);

    void deleteByUserIdAndDeviceId(Long userId, Long deviceId);

    @Modifying
    @Query("DELETE FROM UserDevice ud WHERE ud.userId = :userId")
    void deleteByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserDevice ud WHERE ud.deviceId = :deviceId")
    void deleteByDeviceId(Long deviceId);
}