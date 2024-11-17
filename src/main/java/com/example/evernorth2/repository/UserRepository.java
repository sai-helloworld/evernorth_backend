package com.example.evernorth2.repository;

import com.example.evernorth2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u.member_id FROM User u WHERE u.email_id = :emailId")
    Long findMemberIdByEmail(@Param("emailId") String emailId);
}
