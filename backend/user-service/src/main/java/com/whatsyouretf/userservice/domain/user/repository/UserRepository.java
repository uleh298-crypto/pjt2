package com.whatsyouretf.userservice.domain.user.repository;

import com.whatsyouretf.userservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.socialAccounts WHERE u.id = :id")
    Optional<User> findByIdWithSocialAccounts(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.socialAccounts WHERE u.email = :email")
    Optional<User> findByEmailWithSocialAccounts(@Param("email") String email);
}
