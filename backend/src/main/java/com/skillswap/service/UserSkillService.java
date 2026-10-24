package com.skillswap.service;

import com.skillswap.dto.UserSkillRequest;
import com.skillswap.dto.UserSkillResponse;
import com.skillswap.dto.UserSkillUpdateRequest;
import com.skillswap.entity.Skill;
import com.skillswap.entity.User;
import com.skillswap.entity.UserSkill;
import com.skillswap.enums.SkillType;
import com.skillswap.exception.ConflictException;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.repository.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final UserService userService;
    private final SkillService skillService;

    public UserSkillService(UserSkillRepository userSkillRepository, UserService userService,
                            SkillService skillService) {
        this.userSkillRepository = userSkillRepository;
        this.userService = userService;
        this.skillService = skillService;
    }

    @Transactional(readOnly = true)
    public List<UserSkillResponse> list(String email, SkillType type) {
        User user = userService.getByEmail(email);
        List<UserSkill> skills = type == null
                ? userSkillRepository.findByUserId(user.getId())
                : userSkillRepository.findByUserIdAndType(user.getId(), type);
        return skills.stream().map(UserSkillResponse::from).toList();
    }

    @Transactional
    public UserSkillResponse add(String email, UserSkillRequest request) {
        User user = userService.getByEmail(email);
        Skill skill = skillService.find(request.skillId());
        if (userSkillRepository.existsByUserIdAndSkillIdAndType(user.getId(), skill.getId(), request.type())) {
            throw new ConflictException("You have already added this skill to this list");
        }
        UserSkill saved = userSkillRepository.save(
                new UserSkill(user, skill, request.type(), request.level(), request.notes()));
        return UserSkillResponse.from(saved);
    }

    @Transactional
    public UserSkillResponse update(String email, Long id, UserSkillUpdateRequest request) {
        UserSkill userSkill = findMine(email, id);
        if (request.type() != null && request.type() != userSkill.getType()) {
            if (userSkillRepository.existsByUserIdAndSkillIdAndType(userSkill.getUser().getId(), userSkill.getSkill().getId(), request.type())) {
                String typeName = request.type() == SkillType.OFFERED ? "I can teach" : "I want to learn";
                throw new ConflictException("You have already added this skill to " + typeName);
            }
            userSkill.setType(request.type());
        }
        userSkill.setLevel(request.level());
        userSkill.setNotes(request.notes());
        return UserSkillResponse.from(userSkillRepository.save(userSkill));
    }

    @Transactional
    public void delete(String email, Long id) {
        userSkillRepository.delete(findMine(email, id));
    }

    private UserSkill findMine(String email, Long id) {
        User user = userService.getByEmail(email);
        return userSkillRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill entry not found"));
    }
}
