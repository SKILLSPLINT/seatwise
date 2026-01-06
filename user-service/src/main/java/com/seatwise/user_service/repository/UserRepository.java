package com.seatwise.user_service.repository;

import com.seatwise.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Checks if a user exists with the given email OR phone number.
     * More efficient than checking both separately.
     *
     * @param email the email to check
     * @param phoneNumber the phone number to check
     * @return true if a user exists with either email or phone number
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email OR u.phoneNumber = :phoneNumber")
    boolean existsByEmailOrPhoneNumber(@Param("email") String email, @Param("phoneNumber") String phoneNumber);
}
