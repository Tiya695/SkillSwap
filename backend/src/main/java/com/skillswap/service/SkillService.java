package com.skillswap.service;

import com.skillswap.dto.PageResponse;
import com.skillswap.dto.SkillRequest;
import com.skillswap.dto.SkillResponse;
import com.skillswap.entity.Skill;
import com.skillswap.exception.ConflictException;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.repository.SkillRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public PageResponse<SkillResponse> list(String category, String q, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                Sort.by("category").and(Sort.by("name")));
        return PageResponse.of(skillRepository.search(clean(category), clean(q), pageable), SkillResponse::from);
    }

    public List<String> categories() {
        return skillRepository.findCategories();
    }

    public SkillResponse get(Long id) {
        return SkillResponse.from(find(id));
    }

    public SkillResponse create(SkillRequest request) {
        if (skillRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("A skill with this name already exists");
        }
        Skill skill = new Skill(request.name().trim(), request.category().trim(), request.description());
        return SkillResponse.from(skillRepository.save(skill));
    }

    public SkillResponse update(Long id, SkillRequest request) {
        Skill skill = find(id);
        boolean nameChanged = !skill.getName().equalsIgnoreCase(request.name().trim());
        if (nameChanged && skillRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("A skill with this name already exists");
        }
        skill.setName(request.name().trim());
        skill.setCategory(request.category().trim());
        skill.setDescription(request.description());
        return SkillResponse.from(skillRepository.save(skill));
    }

    public void delete(Long id) {
        skillRepository.delete(find(id));
        skillRepository.flush();
    }

    public Skill find(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
