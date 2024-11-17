package com.example.evernorth2.controller;

import com.example.evernorth2.model.User;
import com.example.evernorth2.repository.UserRepository;
import com.example.evernorth2.service.EmailService;
import com.example.evernorth2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    @PostMapping("/create")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);  // This will insert data into the Users table
    }

    @GetMapping("/get-member-id")
    public ResponseEntity<?> getMemberIdByEmail(@RequestParam String email_id) {
        try {
            Long memberId = userService.findMemberIdByEmail(email_id);
            if (memberId != null) {
                return ResponseEntity.ok().body(memberId);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member ID not found for the provided email.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam Long member_id,
                          @RequestParam String date_of_birth,
                          @RequestParam String email_id) {
        try {
            // Fetch the user record from the database using the instance of userRepository
            Optional<User> user = userRepository.findById(member_id);

            if (user.isEmpty()) {
                return "Error: User with member_id " + member_id + " not found.";
            }

            User existingUser = user.get();

            // Validate date_of_birth and email_id
            if (!existingUser.getDate_of_birth().toString().equals(date_of_birth)) {
                return "Error: Date of birth does not match.";
            }

            if (!existingUser.getEmail_id().equals(email_id)) {
                return "Error: Email ID does not match.";
            }

            // Generate a 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(999999));

            // Store OTP and the current timestamp
            existingUser.setOtp(otp);  // Assuming you added `otp` and `otpTimestamp` fields to your `User` entity
            existingUser.setOtpTimestamp(Instant.now());

            // Save the user with the OTP and timestamp
            userRepository.save(existingUser);

            // Custom message
            String message = "Dear " + existingUser.getFull_name() + ",\n\nYour OTP is: " + otp +
                    ".\n\nPlease use this OTP to verify your details.\n\nBest regards,\nYour Team";

            // Send the OTP via email
            emailService.sendEmail(email_id, "Your OTP Verification", message);

            return "OTP sent successfully to " + email_id;
        } catch (Exception e) {
            return "An error occurred: " + e.getMessage();
        }
    }

    @PostMapping("/generate-otp")
    public String generateOtp(@RequestParam Long member_id,
                              @RequestParam String date_of_birth,
                              @RequestParam String email_id) {
        System.out.println("Received member_id: " + member_id);

        try {
            // Check if user exists
            Optional<User> userOptional = userRepository.findById(member_id);
            if (userOptional.isEmpty()) {
                return "Error: User with Member ID " + member_id + " not found.";
            }

            User user = userOptional.get();

            // Validate date of birth
            if (!user.getDate_of_birth().toString().equals(date_of_birth)) {
                return "Error: Date of birth does not match.";
            }

            // Validate email
            if (!user.getEmail_id().equals(email_id)) {
                return "Error: Email ID does not match.";
            }

            // Generate 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(999999));

            // Store OTP and timestamp in the user
            user.setOtp(otp);  // Assuming you've added `otp` and `otpTimestamp` fields to your User entity
            user.setOtpTimestamp(Instant.now());  // Save the current time when OTP is generated
            userRepository.save(user);

            // Send email with OTP
            String message = "Dear " + user.getFull_name() + ",\n\nYour OTP is: " + otp +
                    ".\n\nPlease use this OTP to verify your login.\n\nBest regards,\nYour Team";
            emailService.sendEmail(email_id, "Your OTP Verification", message);

            return "OTP sent successfully to " + email_id;

        } catch (Exception e) {
            return "An error occurred: " + e.getMessage();
        }
    }

    @PostMapping("/validate-otp")
    public String validateOtp(@RequestParam Long member_id,
                              @RequestParam String email_id,
                              @RequestParam String otp) {

        try {
            // Check if user exists
            Optional<User> userOptional = userRepository.findById(member_id);
            if (userOptional.isEmpty()) {
                return "Error: User with Member ID " + member_id + " not found.";
            }

            User user = userOptional.get();

            // Validate email
            if (!user.getEmail_id().equals(email_id)) {
                return "Error: Email ID does not match.";
            }

            // Validate OTP
            if (!user.getOtp().equals(otp)) {
                return "Error: Invalid OTP.";
            }

            // Check if OTP has expired (30 seconds validity)
            Duration duration = Duration.between(user.getOtpTimestamp(), Instant.now());
            if (duration.getSeconds() > 100) {
                return "Error: OTP expired.";
            }

            return "Validation successful!";

        } catch (Exception e) {
            return "An error occurred: " + e.getMessage();
        }
    }
}
