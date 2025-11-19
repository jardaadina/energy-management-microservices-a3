package com.energy.device.repository;

import com.energy.device.entity.UserReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReferenceRepository extends JpaRepository<UserReference, Long> {
}