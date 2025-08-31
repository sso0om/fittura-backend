package com.fittura.domain.member.controller;

import com.fittura.domain.member.entity.Member;
import com.fittura.domain.member.error.MemberError;
import com.fittura.domain.member.repository.MemberRepository;
import com.fittura.global.config.AppProperties;
import com.fittura.global.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String SIGN_UP_URL = "/api/v1/auth/signup";
    private static final String SIGN_IN_URL = "/api/v1/auth/signin";
    private static final String REISSUE_URL = "/api/v1/auth/reissue";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";

    @Test
    @DisplayName("회원가입 성공")
    void signUpSuccess() throws Exception {
        // given
        String reqBody = """
             {
                 "email": "test@email.com",
                 "name": "테스트 유저",
                 "nickname": "테스터1",
                 "password": "password123!"
             }
             """;

        // when & then
        ResultActions resultActions = mockMvc
            .perform(post(SIGN_UP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        Member member = memberRepository.findByEmail("test@email.com")
            .orElseThrow(() -> new AssertionError("회원가입 후 이메일로 회원을 조회하지 못했습니다."));

        // verify
        resultActions
            .andExpect(handler().handlerType(AuthControllerV1.class))
            .andExpect(handler().methodName("signUp"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));

        verifyAuthDataAndCookie(resultActions, member);
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @DisplayName("회원가입 실패 - 중복 이메일/닉네임")
    @MethodSource("duplicateSignUpInfoProvider")
    void signUpFailWithDuplication(String reqBody, String testName, MemberError error) throws Exception {
        // given
        Member existingMember = Member.createUser(
            "test@email.com",
            "유저",
            "테스트 유저",
            passwordEncoder.encode("password123!")
        );
        memberRepository.save(existingMember);

        // when
        ResultActions resultActions = mockMvc
            .perform(post(SIGN_UP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify & then
        verifyAuthFailure(resultActions, "signUp", error);
    }

    @Test
    @DisplayName("로그인 성공")
    void signInSuccess() throws Exception {
        // given
        Member member = Member.createUser(
            "test@email.com",
            "유저",
            "테스트 유저",
            passwordEncoder.encode("password123!")
        );
        memberRepository.save(member);
        
        String reqBody = """
             {
                 "email": "test@email.com",
                 "password": "password123!"
             }
             """;

        // when & then
        ResultActions resultActions = mockMvc
            .perform(post(SIGN_IN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        resultActions
            .andExpect(handler().handlerType(AuthControllerV1.class))
            .andExpect(handler().methodName("signIn"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("로그인되었습니다."));

        verifyAuthDataAndCookie(resultActions, member);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void signInFailWithNotExistingEmail() throws Exception {
        // given
        Member member = Member.createUser(
            "test@email.com",
            "유저",
            "테스트 유저",
            passwordEncoder.encode("password123!")
        );
        memberRepository.save(member);

        String reqBody = """
             {
                 "email": "otherTest@email.com",
                 "password": "password123!"
             }
             """;

        // when & then
        ResultActions resultActions = mockMvc
            .perform(post(SIGN_IN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        verifyAuthFailure(resultActions, "signIn", MemberError.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void signInFailWithWrongPassword() throws Exception {
        // given
        Member member = Member.createUser(
            "test@email.com",
            "유저",
            "테스트 유저",
            passwordEncoder.encode("password123!")
        );
        memberRepository.save(member);

        String reqBody = """
             {
                 "email": "test@email.com",
                 "password": "password"
             }
             """;

        // when & then
        ResultActions resultActions = mockMvc
            .perform(post(SIGN_IN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        verifyAuthFailure(resultActions, "signIn", MemberError.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissueSuccess() throws Exception {
        // given
        Member member = Member.createUser(
            "test@email.com",
            "유저",
            "테스트 유저",
            passwordEncoder.encode("password123!")
        );
        memberRepository.save(member);

        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId().toString());
        String tokenName = appProperties.cookie().refreshTokenName();
        Cookie refreshTokenCookie = new Cookie(tokenName, refreshToken);

        // when & then
        ResultActions resultActions = mockMvc
            .perform(post(REISSUE_URL)
                .cookie(refreshTokenCookie)
            )
            .andDo(print());

        // verify
        resultActions
            .andExpect(handler().handlerType(AuthControllerV1.class))
            .andExpect(handler().methodName("reissueTokens"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("토큰이 재발급되었습니다."))
            .andExpect(cookie().value(tokenName, not(equalTo(refreshToken)))); // 새 토큰이 발급되었는지 확인

        verifyAuthDataAndCookie(resultActions, member);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logoutSuccess() throws Exception {
        // given
        String tokenName = appProperties.cookie().refreshTokenName();

        // when
        ResultActions resultActions = mockMvc
            .perform(post(LOGOUT_URL))
            .andDo(print());

        resultActions
            .andExpect(handler().handlerType(AuthControllerV1.class))
            .andExpect(handler().methodName("logout"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("로그아웃되었습니다."))
            .andExpect(cookie().exists(tokenName))
            .andExpect(cookie().value(tokenName, ""))
            .andExpect(cookie().maxAge(tokenName, 0))
            .andExpect(cookie().httpOnly(tokenName, appProperties.cookie().httpOnly()))
            .andExpect(cookie().secure(tokenName, appProperties.cookie().secure()))
            .andExpect(cookie().sameSite(tokenName, appProperties.cookie().sameSite()))
            .andExpect(cookie().path(tokenName, appProperties.cookie().path()));
    }


    // ========== Arguments Provider ==========

    private static Stream<Arguments> duplicateSignUpInfoProvider() {
        return Stream.of(
            Arguments.of(
                """
                 {"email":"test@email.com","name":"유저","nickname":"다른 유저","password":"password123!"}
                 """,
                "중복 이메일",
                MemberError.DUPLICATED_EMAIL
            ),
            Arguments.of(
                """
                 {"email":"otherTest@email.com","name":"유저","nickname":"테스트 유저","password":"password123!"}
                 """,
                "중복 닉네임",
                MemberError.DUPLICATED_NICKNAME
            )
        );
    }


    // ========== 헬퍼 메서드 ==========

    private void verifyAuthDataAndCookie(ResultActions resultActions, Member member) throws Exception {
        String tokenName = appProperties.cookie().refreshTokenName();
        int maxAge = (int) (jwtTokenProvider.getRefreshTokenValidityInMilliseconds() / 1000);

        resultActions
            .andExpect(jsonPath("$.data.id").value(member.getId()))
            .andExpect(jsonPath("$.data.email").value(member.getEmail()))
            .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(cookie().exists(tokenName))
            .andExpect(cookie().httpOnly(tokenName, appProperties.cookie().httpOnly()))
            .andExpect(cookie().secure(tokenName, appProperties.cookie().secure()))
            .andExpect(cookie().sameSite(tokenName, appProperties.cookie().sameSite()))
            .andExpect(cookie().path(tokenName, appProperties.cookie().path()))
            .andExpect(cookie().maxAge(tokenName, maxAge));
    }

    private static void verifyAuthFailure(ResultActions resultActions, String methodName, MemberError error) throws Exception {
        resultActions
            .andExpect(handler().handlerType(AuthControllerV1.class))
            .andExpect(handler().methodName(methodName))
            .andExpect(status().is(error.getStatus()))
            .andExpect(jsonPath("$.status").value(error.getStatus()))
            .andExpect(jsonPath("$.code").value(error.getCode()))
            .andExpect(jsonPath("$.message").value(error.getMessage()));
    }
}