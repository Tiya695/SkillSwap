package com.skillswap.service;

import com.skillswap.dto.PublicProfileResponse;
import com.skillswap.dto.UpdateProfileRequest;
import com.skillswap.dto.UserResponse;
import com.skillswap.dto.UserSkillResponse;
import com.skillswap.dto.UserSummary;
import com.skillswap.entity.User;
import com.skillswap.entity.UserSkill;
import com.skillswap.enums.SkillType;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.repository.ReviewRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.repository.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorage;

    public UserService(UserRepository userRepository, UserSkillRepository userSkillRepository,
                       ReviewRepository reviewRepository, FileStorageService fileStorage) {
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.reviewRepository = reviewRepository;
        this.fileStorage = fileStorage;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserResponse getMe(String email) {
        return UserResponse.from(getByEmail(email));
    }

    @Transactional
    public UserResponse updateMe(String email, UpdateProfileRequest request) {
        User user = getByEmail(email);
        user.setName(request.name().trim());
        user.setBio(blankToNull(request.bio()));
        user.setDateOfBirth(request.dateOfBirth());
        user.setPhone(PhoneUtil.normalize(request.phone()));
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateAvatar(String email, MultipartFile file) {
        User user = getByEmail(email);
        String name = fileStorage.saveAvatar(file);
        String oldUrl = user.getAvatarUrl();
        user.setAvatarUrl(FileStorageService.AVATAR_URL_PREFIX + name);
        User saved = userRepository.save(user);
        fileStorage.deleteByUrl(oldUrl);
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse removeAvatar(String email) {
        User user = getByEmail(email);
        String oldUrl = user.getAvatarUrl();
        user.setAvatarUrl(null);
        User saved = userRepository.save(user);
        fileStorage.deleteByUrl(oldUrl);
        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<UserSkill> skills = userSkillRepository.findByUserId(id);
        Double avg = reviewRepository.averageRating(id);
        return new PublicProfileResponse(
                UserSummary.from(user),
                user.getCreatedAt(),
                skills.stream().filter(s -> s.getType() == SkillType.OFFERED).map(UserSkillResponse::from).toList(),
                skills.stream().filter(s -> s.getType() == SkillType.WANTED).map(UserSkillResponse::from).toList(),
                avg == null ? null : Math.round(avg * 10.0) / 10.0,
                reviewRepository.countByRevieweeId(id));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
