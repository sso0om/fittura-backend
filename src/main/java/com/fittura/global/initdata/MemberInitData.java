package com.fittura.global.initdata;

import com.fittura.domain.member.entity.Member;
import com.fittura.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class MemberInitData implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (memberRepository.count() > 0) {
            return;
        }

        Member admin = Member.createAdmin(
            "admin@test.com",
            "관리자",
            "admin",
            passwordEncoder.encode("admin1234!")
        );

        Member user1 = Member.createUser(
            "user1@test.com",
            "사용자1",
            "user1",
            passwordEncoder.encode("user1234!")
        );

        Member user2 = Member.createUser(
            "user2@test.com",
            "사용자2",
            "user2",
            passwordEncoder.encode("user1234!")
        );

        memberRepository.save(admin);
        memberRepository.save(user1);
        memberRepository.save(user2);
    }
}
