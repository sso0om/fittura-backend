package com.fittura.domain.member.service;

import com.fittura.domain.member.entity.Member;
import com.fittura.domain.member.error.AuthError;
import com.fittura.domain.member.repository.MemberRepository;
import com.fittura.global.error.CommonErrorCode;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new ServiceException(AuthError.INVALID_CREDENTIALS));
    }

    @Transactional
    public Member createMember(Member member) {
        try {
            return memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            // 동시성 문제로 인한 DB 유니크 제약 위반 처리
            validateSignUpRequest(member.getEmail(), member.getNickname());
            log.error("Unknown DataIntegrityViolationException during sign up: email={}, nickname={}", member.getEmail(), member.getNickname(), e);
            throw new ServiceException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    // ========== 유효성 검사 메서드 ==========

    public void validateSignUpRequest(String email, String nickname) {
        validateDuplicatedEmail(email);
        validateDuplicatedNickname(nickname);
    }

    public void validateDuplicatedEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new ServiceException(AuthError.DUPLICATED_EMAIL);
        }
    }

    public void validateDuplicatedNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new ServiceException(AuthError.DUPLICATED_NICKNAME);
        }
    }
}
