package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("SELECT a FROM Address a WHERE a.user.userId = ?1")
    List<Address> findByUserId(Long userId);
}
