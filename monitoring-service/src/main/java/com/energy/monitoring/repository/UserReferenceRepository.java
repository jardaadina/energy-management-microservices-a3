package com.energy.monitoring.repository;

import com.energy.monitoring.entity.UserReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReferenceRepository extends JpaRepository<UserReference, Long> {
}