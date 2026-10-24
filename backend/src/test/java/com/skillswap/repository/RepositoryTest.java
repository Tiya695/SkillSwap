package com.skillswap.repository;

import com.skillswap.entity.Skill;
import com.skillswap.entity.User;
import com.skillswap.entity.UserSkill;
import com.skillswap.enums.SkillLevel;
import com.skillswap.enums.SkillType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    SkillRepository skillRepository;
    @Autowired
    UserSkillRepository userSkillRepository;

    User tiya;
    User rahul;
    Skill python;
    Skill guitar;

    @BeforeEach
    void setUp() {
        tiya = saveUser("Tiya", "tiya@test.com");
        rahul = saveUser("Rahul", "rahul@test.com");
        python = skillRepository.save(new Skill("Python", "Programming", "Scripting"));
        guitar = skillRepository.save(new Skill("Guitar", "Music", "Strings"));
        userSkillRepository.save(new UserSkill(tiya, python, SkillType.OFFERED, SkillLevel.ADVANCED, null));
        userSkillRepository.save(new UserSkill(tiya, guitar, SkillType.WANTED, SkillLevel.BEGINNER, null));
        userSkillRepository.save(new UserSkill(rahul, guitar, SkillType.OFFERED, SkillLevel.ADVANCED, null));
        userSkillRepository.save(new UserSkill(rahul, python, SkillType.WANTED, SkillLevel.BEGINNER, null));
    }

    private User saveUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword("hash");
        return userRepository.save(u);
    }

    @Test
    void findAllOfOtherUsersExcludesTheCaller() {
        var rows = userSkillRepository.findAllOfOtherUsers(tiya.getId());

        assertThat(rows).hasSize(2);
        assertThat(rows).allMatch(us -> us.getUser().getId().equals(rahul.getId()));
        assertThat(rows).extracting(us -> us.getSkill().getName()).containsExactlyInAnyOrder("Guitar", "Python");
    }

    @Test
    void findByUserIdAndTypeFiltersByType() {
        assertThat(userSkillRepository.findByUserIdAndType(tiya.getId(), SkillType.OFFERED))
                .extracting(us -> us.getSkill().getName()).containsExactly("Python");
    }

    @Test
    void existsChecksUserSkillAndType() {
        assertThat(userSkillRepository.existsByUserIdAndSkillIdAndType(tiya.getId(), python.getId(), SkillType.OFFERED)).isTrue();
        assertThat(userSkillRepository.existsByUserIdAndSkillIdAndType(tiya.getId(), python.getId(), SkillType.WANTED)).isFalse();
    }

    @Test
    void skillSearchFiltersByCategoryAndText() {
        assertThat(skillRepository.search("", "", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(skillRepository.search("Music", "", PageRequest.of(0, 10)).getContent())
                .extracting(Skill::getName).containsExactly("Guitar");
        assertThat(skillRepository.search("", "pyth", PageRequest.of(0, 10)).getContent())
                .extracting(Skill::getName).containsExactly("Python");
        assertThat(skillRepository.findCategories()).containsExactly("Music", "Programming");
    }
}
