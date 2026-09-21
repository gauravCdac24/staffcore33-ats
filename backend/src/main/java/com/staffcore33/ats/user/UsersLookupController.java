package com.staffcore33.ats.user;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersLookupController {

    private final UserRepository userRepository;

    @GetMapping("/lookup")
    @Transactional(readOnly = true)
    public List<UserLookupResponse> lookup(@RequestParam(required = false) UserRole role) {
        return userRepository.findAllActive().stream()
                .filter(u -> role == null || u.getRole() == role)
                .filter(User::isActive)
                .map(u -> UserLookupResponse.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .role(u.getRole())
                        .build())
                .toList();
    }
}
