package com.skillswap;

import com.skillswap.entity.Skill;
import com.skillswap.entity.SwapRequest;
import com.skillswap.entity.User;
import com.skillswap.entity.UserSkill;
import com.skillswap.enums.SkillLevel;
import com.skillswap.enums.SkillType;
import com.skillswap.enums.SwapStatus;

public final class TestData {

    private TestData() {
    }

    public static User user(Long id, String name) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(name.toLowerCase() + "@test.com");
        u.setPassword("hash");
        return u;
    }

    public static Skill skill(Long id, String name) {
        Skill s = new Skill(name, "General", null);
        s.setId(id);
        return s;
    }

    public static UserSkill userSkill(User user, Skill skill, SkillType type) {
        return new UserSkill(user, skill, type, SkillLevel.BEGINNER, null);
    }

    public static SwapRequest swap(Long id, User requester, User receiver, Skill offered, Skill wanted,
                                   SwapStatus status) {
        SwapRequest s = new SwapRequest();
        s.setId(id);
        s.setRequester(requester);
        s.setReceiver(receiver);
        s.setSkillOffered(offered);
        s.setSkillWanted(wanted);
        s.setStatus(status);
        return s;
    }
}
