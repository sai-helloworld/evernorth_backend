package com.example.evernorth2.service;

import com.example.evernorth2.model.User;
import com.example.evernorth2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);  // Save the user, the `member_id` will be auto-generated
    }
    public Long findMemberIdByEmail(String email) {
        return userRepository.findMemberIdByEmail(email);
    }

}
