package com.skillswap.service;

import com.skillswap.entity.Assessment;
import com.skillswap.entity.User;
import com.skillswap.enums.Role;
import com.skillswap.exception.BadRequestException;
import com.skillswap.exception.ForbiddenException;
import com.skillswap.repository.AssessmentRepository;
import com.skillswap.repository.ReviewRepository;
import com.skillswap.repository.SwapMessageRepository;
import com.skillswap.repository.SwapRequestRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.repository.UserSkillRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AccountService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final SwapRequestRepository swapRepository;
    private final SwapMessageRepository messageRepository;
    private final AssessmentRepository assessmentRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorage;

    public AccountService(UserService userService, UserRepository userRepository,
                          UserSkillRepository userSkillRepository, SwapRequestRepository swapRepository,
                          SwapMessageRepository messageRepository, AssessmentRepository assessmentRepository,
                          ReviewRepository reviewRepository, PasswordEncoder passwordEncoder,
                          FileStorageService fileStorage) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.swapRepository = swapRepository;
        this.messageRepository = messageRepository;
        this.assessmentRepository = assessmentRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorage = fileStorage;
    }

    @Transactional
    public void deleteAccount(String email, String password) {
        User user = userService.getByEmail(email);
        if (user.getRole() == Role.ADMIN) {
            throw new ForbiddenException("The admin account cannot be deleted");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("The password is not correct", Map.of("password", "The password is not correct"));
        }

        List<Long> swapIds = swapRepository.findIdsByParticipant(user.getId());
        if (!swapIds.isEmpty()) {
            reviewRepository.deleteBySwapIds(swapIds);
            messageRepository.deleteBySwapIds(swapIds);
            List<Assessment> assessments = assessmentRepository.findBySwapRequestIdIn(swapIds);
            assessmentRepository.deleteAll(assessments);
            assessmentRepository.flush();
            swapRepository.deleteAllByIdInBatch(swapIds);
        }
        userSkillRepository.deleteByUserId(user.getId());
        String avatarUrl = user.getAvatarUrl();
        userRepository.delete(user);
        userRepository.flush();
        fileStorage.deleteByUrl(avatarUrl);
    }
}
