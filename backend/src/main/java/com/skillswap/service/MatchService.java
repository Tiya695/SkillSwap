package com.skillswap.service;

import com.skillswap.dto.MatchResponse;
import com.skillswap.dto.SkillResponse;
import com.skillswap.dto.UserSummary;
import com.skillswap.entity.Skill;
import com.skillswap.entity.User;
import com.skillswap.entity.UserSkill;
import com.skillswap.enums.MatchType;
import com.skillswap.enums.SkillType;
import com.skillswap.repository.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final UserSkillRepository userSkillRepository;
    private final UserService userService;

    public MatchService(UserSkillRepository userSkillRepository, UserService userService) {
        this.userSkillRepository = userSkillRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findMatches(String email) {
        User me = userService.getByEmail(email);
        List<UserSkill> mine = userSkillRepository.findByUserId(me.getId());

        Set<Long> iOffer = skillIds(mine, SkillType.OFFERED);
        Set<Long> iWant = skillIds(mine, SkillType.WANTED);

        Map<Long, List<UserSkill>> byUser = new LinkedHashMap<>();
        for (UserSkill us : userSkillRepository.findAllOfOtherUsers(me.getId())) {
            byUser.computeIfAbsent(us.getUser().getId(), id -> new ArrayList<>()).add(us);
        }

        List<MatchResponse> matches = new ArrayList<>();
        for (List<UserSkill> theirs : byUser.values()) {
            User other = theirs.get(0).getUser();

            List<Skill> theyCanTeachYou = theirs.stream()
                    .filter(s -> s.getType() == SkillType.OFFERED && iWant.contains(s.getSkill().getId()))
                    .map(UserSkill::getSkill).toList();

            List<Skill> youCanTeachThem = theirs.stream()
                    .filter(s -> s.getType() == SkillType.WANTED && iOffer.contains(s.getSkill().getId()))
                    .map(UserSkill::getSkill).toList();

            if (theyCanTeachYou.isEmpty() && youCanTeachThem.isEmpty()) {
                continue;
            }
            MatchType type = !theyCanTeachYou.isEmpty() && !youCanTeachThem.isEmpty()
                    ? MatchType.MUTUAL : MatchType.ONE_WAY;
            matches.add(new MatchResponse(
                    UserSummary.from(other), type,
                    theyCanTeachYou.stream().map(SkillResponse::from).toList(),
                    youCanTeachThem.stream().map(SkillResponse::from).toList(),
                    theyCanTeachYou.size() + youCanTeachThem.size()));
        }

        matches.sort(Comparator.comparing(MatchResponse::matchType)
                .thenComparing(Comparator.comparingInt(MatchResponse::score).reversed())
                .thenComparing(m -> m.user().name()));
        return matches;
    }

    private Set<Long> skillIds(List<UserSkill> skills, SkillType type) {
        return skills.stream().filter(s -> s.getType() == type)
                .map(s -> s.getSkill().getId()).collect(Collectors.toSet());
    }
}
