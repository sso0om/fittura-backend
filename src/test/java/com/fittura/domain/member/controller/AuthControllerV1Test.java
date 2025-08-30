package com.fittura.domain.member.controller;

import com.fittura.domain.member.entity.Member;
import com.fittura.domain.member.error.AuthError;
import com.fittura.domain.member.repository.MemberRepository;
import com.fittura.global.config.AppProperties;
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
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String SIGN_UP_URL = "/api/v1/auth/signup";
    private static final String SIGN_IN_URL = "/api/v1/auth/signin";

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
        String tokenName = appProperties.cookie().refreshTokenName();

        // verify
        resultActions
            .andExpect(handler().handlerType(AuthControllerV1.class))
            .andExpect(handler().methodName("signUp"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
            .andExpect(jsonPath("$.data.id").value(member.getId()))
            .andExpect(jsonPath("$.data.email").value(member.getEmail()))
            .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(cookie().exists(tokenName))
            .andExpect(cookie().httpOnly(tokenName, appProperties.cookie().httpOnly()))
            .andExpect(cookie().secure(tokenName, appProperties.cookie().secure()))
            .andExpect(cookie().sameSite(tokenName, appProperties.cookie().sameSite()));
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @DisplayName("회원가입 실패 - 중복 이메일/닉네임")
    @MethodSource("duplicateSignUpInfoProvider")
    void signUpFailWithDuplicationEmail(String reqBody, String testName, String errorCode, String errorMessage) throws Exception {
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

        // then
        resultActions
            .andExpect(handler().handlerType(AuthControllerV1.class))
            .andExpect(handler().methodName("signUp"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.code").value(errorCode))
            .andExpect(jsonPath("$.message").value(errorMessage));
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

        String tokenName = appProperties.cookie().refreshTokenName();

        // verify
        resultActions
            .andExpect(handler().handlerType(AuthControllerV1.class))
            .andExpect(handler().methodName("signIn"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("로그인되었습니다."))
            .andExpect(jsonPath("$.data.id").value(member.getId()))
            .andExpect(jsonPath("$.data.email").value(member.getEmail()))
            .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(cookie().exists(tokenName))
            .andExpect(cookie().httpOnly(tokenName, appProperties.cookie().httpOnly()))
            .andExpect(cookie().secure(tokenName, appProperties.cookie().secure()))
            .andExpect(cookie().sameSite(tokenName, appProperties.cookie().sameSite()));
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

        String tokenName = appProperties.cookie().refreshTokenName();

        // verify
        resultActions
            .andExpect(handler().handlerType(AuthControllerV1.class))
            .andExpect(handler().methodName("signIn"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.code").value("A401-01"))
            .andExpect(jsonPath("$.message").value(AuthError.INVALID_CREDENTIALS.getMessage()));
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

        String tokenName = appProperties.cookie().refreshTokenName();

        // verify
        resultActions
            .andExpect(handler().handlerType(AuthControllerV1.class))
            .andExpect(handler().methodName("signIn"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.code").value("A401-01"))
            .andExpect(jsonPath("$.message").value(AuthError.INVALID_CREDENTIALS.getMessage()));
    }


    // ========== Arguments Provider ==========

    private static Stream<Arguments> duplicateSignUpInfoProvider() {
        return Stream.of(
            Arguments.of(
                """
                 {"email":"test@email.com","name":"유저","nickname":"다른 유저","password":"password123!"}
                 """,
                "중복 이메일",
                "A409-01",
                "이미 사용중인 이메일입니다."
            ),
            Arguments.of(
                """
                 {"email":"otherTest@email.com","name":"유저","nickname":"테스트 유저","password":"password123!"}
                 """,
                "중복 닉네임",
                "A409-02",
                "이미 사용중인 닉네임입니다."
            )
        );
    }
}