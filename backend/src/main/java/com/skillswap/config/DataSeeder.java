package com.skillswap.config;

import com.skillswap.entity.Skill;
import com.skillswap.entity.User;
import com.skillswap.entity.UserSkill;
import com.skillswap.enums.Role;
import com.skillswap.enums.SkillLevel;
import com.skillswap.enums.SkillType;
import com.skillswap.repository.SkillRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.repository.UserSkillRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final LocalDate adminDateOfBirth;
    private final boolean seedDemoUsers;

    public DataSeeder(UserRepository userRepository, SkillRepository skillRepository,
                      UserSkillRepository userSkillRepository, PasswordEncoder passwordEncoder,
                      @Value("${app.admin.password:Admin@123}") String adminPassword,
                      @Value("${app.admin.date-of-birth:2000-01-01}") String adminDateOfBirth,
                      @Value("${app.seed.demo-users:false}") boolean seedDemoUsers) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.adminDateOfBirth = LocalDate.parse(adminDateOfBirth);
        this.seedDemoUsers = seedDemoUsers;
    }

    @Override
    public void run(String... args) {
        if (skillRepository.count() == 0) {
            seedSkills();
        }
        if (!userRepository.existsByEmail("admin@skillswap.com")) {
            User admin = user("Admin", "admin@skillswap.com", adminPassword, Role.ADMIN, "Keeps the skill catalog tidy.");
            admin.setDateOfBirth(adminDateOfBirth);
            userRepository.save(admin);
        }
        if (seedDemoUsers && userRepository.count() == 1) {
            seedUsers();
        }
    }

    private void seedSkills() {
        skillRepository.saveAll(List.of(
                new Skill("Python", "Programming", "Scripting, automation and data work"),
                new Skill("Java", "Programming", "Object oriented programming and Spring Boot"),
                new Skill("JavaScript", "Programming", "Programming for the web"),
                new Skill("Guitar", "Music", "Acoustic and electric guitar"),
                new Skill("Piano", "Music", "Keys, reading music and chords"),
                new Skill("Singing", "Music", "Voice control and breathing"),
                new Skill("Spanish", "Language", "Conversation and grammar"),
                new Skill("French", "Language", "Conversation and grammar"),
                new Skill("English Speaking", "Language", "Fluency and pronunciation"),
                new Skill("UI Design", "Design", "Interfaces, layout and prototyping"),
                new Skill("Photography", "Design", "Composition, light and editing"),
                new Skill("Graphic Design", "Design", "Posters, logos and branding"),
                new Skill("Baking", "Cooking", "Bread, cakes and pastry"),
                new Skill("Indian Cooking", "Cooking", "Everyday curries, breads and spices"),
                new Skill("Yoga", "Fitness", "Poses, breathing and flexibility"),
                new Skill("Strength Training", "Fitness", "Gym routines and form"),
                new Skill("Running", "Fitness", "Pacing, endurance and race prep")));
    }

    private void seedUsers() {
        User tiya = user("Tiya Rai", "tiya@gmail.com", "Demo@123", Role.USER,
                "Backend developer. Happy to trade code for music.");
        User rahul = user("Rahul Mehta", "rahul@gmail.com", "Demo@123", Role.USER,
                "Guitarist and weekend photographer.");
        User meera = user("Meera Nair", "meera@gmail.com", "Demo@123", Role.USER,
                "Designer who loves languages.");
        for (User u : List.of(tiya, rahul, meera)) {
            u.setDateOfBirth(LocalDate.of(2000, 1, 1));
        }
        userRepository.saveAll(List.of(tiya, rahul, meera));

        Map<String, Skill> skills = new HashMap<>();
        skillRepository.findAll().forEach(s -> skills.put(s.getName(), s));

        link(tiya, skills.get("Python"), SkillType.OFFERED, SkillLevel.ADVANCED);
        link(tiya, skills.get("Yoga"), SkillType.OFFERED, SkillLevel.BEGINNER);
        link(tiya, skills.get("Guitar"), SkillType.WANTED, SkillLevel.BEGINNER);
        link(tiya, skills.get("Spanish"), SkillType.WANTED, SkillLevel.BEGINNER);

        link(rahul, skills.get("Guitar"), SkillType.OFFERED, SkillLevel.ADVANCED);
        link(rahul, skills.get("Photography"), SkillType.OFFERED, SkillLevel.INTERMEDIATE);
        link(rahul, skills.get("Python"), SkillType.WANTED, SkillLevel.BEGINNER);
        link(rahul, skills.get("UI Design"), SkillType.WANTED, SkillLevel.BEGINNER);

        link(meera, skills.get("Spanish"), SkillType.OFFERED, SkillLevel.ADVANCED);
        link(meera, skills.get("UI Design"), SkillType.OFFERED, SkillLevel.ADVANCED);
        link(meera, skills.get("Python"), SkillType.WANTED, SkillLevel.BEGINNER);
        link(meera, skills.get("Yoga"), SkillType.WANTED, SkillLevel.BEGINNER);
    }

    private User user(String name, String email, String password, Role role, String bio) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(role);
        u.setBio(bio);
        return u;
    }

    private void link(User user, Skill skill, SkillType type, SkillLevel level) {
        userSkillRepository.save(new UserSkill(user, skill, type, level, null));
    }
}
