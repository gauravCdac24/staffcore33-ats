package com.staffcore33.ats.user;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.common.BadRequestException;
import com.staffcore33.ats.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAllActive().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listDeleted() {
        return userRepository.findAllDeleted().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .active(request.getActive() == null || request.getActive())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);
        auditService.log("USER_CREATED", "USER", user.getId(), user.getEmail());
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = findActive(id);
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        auditService.log("USER_UPDATED", "USER", user.getId(), user.getEmail());
        return toResponse(user);
    }

    @Transactional
    public void softDelete(Long id) {
        User user = findActive(id);
        user.setDeletedAt(Instant.now());
        user.setActive(false);
        auditService.log("USER_DELETED", "USER", user.getId(), user.getEmail());
    }

    @Transactional
    public UserResponse restore(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setDeletedAt(null);
        user.setActive(true);
        auditService.log("USER_RESTORED", "USER", user.getId(), user.getEmail());
        return toResponse(user);
    }

    public User findActive(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }
}
