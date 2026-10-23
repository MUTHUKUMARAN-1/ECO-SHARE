package com.econest.repository;

import com.econest.entity.Cart;
import com.econest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findBySessionToken(String sessionToken);

    Optional<Cart> findByUser(User user);
}
