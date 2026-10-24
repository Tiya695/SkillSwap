package com.skillswap.service;

import com.skillswap.dto.MatchResponse;
import com.skillswap.entity.Skill;
import com.skillswap.entity.User;
import com.skillswap.enums.MatchType;
import com.skillswap.enums.SkillType;
import com.skillswap.repository.UserSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.skillswap.TestData.skill;
import static com.skillswap.TestData.user;
import static com.skillswap.TestData.userSkill;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    UserSkillRepository userSkillRepository;
    @Mock
    UserService userService;
    @InjectMocks
    MatchService matchService;

    User me = user(1L, "Me");
    User rahul = user(2L, "Rahul");
    User meera = user(3L, "Meera");
    Skill python = skill(10L, "Python");
    Skill guitar = skill(11L, "Guitar");
    Skill spanish = skill(12L, "Spanish");
    Skill yoga = skill(13L, "Yoga");

    @BeforeEach
    void setUp() {
        when(userService.getByEmail(me.getEmail())).thenReturn(me);
    }

    @Test
    void mutualMatchWhenBothSidesFit() {
        when(userSkillRepository.findByUserId(1L)).thenReturn(List.of(
                userSkill(me, python, SkillType.OFFERED), userSkill(me, guitar, SkillType.WANTED)));
        when(userSkillRepository.findAllOfOtherUsers(1L)).thenReturn(List.of(
                userSkill(rahul, guitar, SkillType.OFFERED), userSkill(rahul, python, SkillType.WANTED)));

        List<MatchResponse> matches = matchService.findMatches(me.getEmail());

        assertThat(matches).hasSize(1);
        MatchResponse m = matches.get(0);
        assertThat(m.matchType()).isEqualTo(MatchType.MUTUAL);
        assertThat(m.theyCanTeachYou()).extracting("name").containsExactly("Guitar");
        assertThat(m.youCanTeachThem()).extracting("name").containsExactly("Python");
        assertThat(m.score()).isEqualTo(2);
    }

    @Test
    void oneWayMatchWhenOnlyOneSideFits() {
        when(userSkillRepository.findByUserId(1L)).thenReturn(List.of(
                userSkill(me, python, SkillType.OFFERED), userSkill(me, guitar, SkillType.WANTED)));

        when(userSkillRepository.findAllOfOtherUsers(1L)).thenReturn(List.of(
                userSkill(rahul, guitar, SkillType.OFFERED), userSkill(rahul, yoga, SkillType.WANTED)));

        List<MatchResponse> matches = matchService.findMatches(me.getEmail());

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).matchType()).isEqualTo(MatchType.ONE_WAY);
        assertThat(matches.get(0).youCanTeachThem()).isEmpty();
    }

    @Test
    void noMatchWhenNothingOverlaps() {
        when(userSkillRepository.findByUserId(1L)).thenReturn(List.of(
                userSkill(me, python, SkillType.OFFERED), userSkill(me, guitar, SkillType.WANTED)));
        when(userSkillRepository.findAllOfOtherUsers(1L)).thenReturn(List.of(
                userSkill(rahul, yoga, SkillType.OFFERED), userSkill(rahul, spanish, SkillType.WANTED)));

        assertThat(matchService.findMatches(me.getEmail())).isEmpty();
    }

    @Test
    void mutualRanksAboveOneWayAndHigherScoreFirst() {
        when(userSkillRepository.findByUserId(1L)).thenReturn(List.of(
                userSkill(me, python, SkillType.OFFERED), userSkill(me, yoga, SkillType.OFFERED),
                userSkill(me, guitar, SkillType.WANTED), userSkill(me, spanish, SkillType.WANTED)));
        when(userSkillRepository.findAllOfOtherUsers(1L)).thenReturn(List.of(

                userSkill(rahul, guitar, SkillType.OFFERED), userSkill(rahul, spanish, SkillType.OFFERED),

                userSkill(meera, guitar, SkillType.OFFERED), userSkill(meera, python, SkillType.WANTED)));

        List<MatchResponse> matches = matchService.findMatches(me.getEmail());

        assertThat(matches).extracting(m -> m.user().name()).containsExactly("Meera", "Rahul");
        assertThat(matches.get(0).matchType()).isEqualTo(MatchType.MUTUAL);
        assertThat(matches.get(1).score()).isEqualTo(2);
    }

    @Test
    void loggedInUserIsNeverInTheirOwnMatches() {
        when(userSkillRepository.findByUserId(1L)).thenReturn(List.of(
                userSkill(me, python, SkillType.OFFERED), userSkill(me, python, SkillType.WANTED)));

        when(userSkillRepository.findAllOfOtherUsers(1L)).thenReturn(List.of());

        assertThat(matchService.findMatches(me.getEmail())).isEmpty();
    }
}
