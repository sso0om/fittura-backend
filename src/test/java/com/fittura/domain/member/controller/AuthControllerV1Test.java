package com.fittura.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittura.domain.member.entity.Member;
import com.fittura.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    private static final String SIGN_UP_URL = "/api/v1/auth/signup";

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

        Member member = memberRepository.findByEmail("test@email.com").get();

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
            .andExpect(cookie().exists("refreshToken"))
            .andExpect(cookie().httpOnly("refreshToken", true))
            .andExpect(cookie().secure("refreshToken", true))
            .andExpect(cookie().sameSite("refreshToken", "Strict"))
            .andExpect(cookie().maxAge("refreshToken", 604800));

    }

    @ParameterizedTest(name = "[{index}] {1}")
    @DisplayName("회원가입 실패 - 중복 이메일/닉네임")
    @MethodSource("duplicateSignUpInfoProvider")
    void signUpFailWithDuplicationEmail(String reqBody, String testName, String errorCode, String errorMessage) throws Exception {
        // given
        Member existingMember = Member.createUser("test@email.com", "유저", "테스터", "password123!");
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

    private static Stream<Arguments> duplicateSignUpInfoProvider() {
        return Stream.of(
            Arguments.of(
                """
                 {"email":"test@email.com","name":"유저","nickname":"유저","password":"password123!"}
                 """,
                "중복 이메일",
                "A409-01",
                "이미 사용중인 이메일입니다."
            ),
            Arguments.of(
                """
                 {"email":"test1@email.com","name":"유저","nickname":"테스터","password":"password123!"}
                 """,
                "중복 닉네임",
                "A409-02",
                "이미 사용중인 닉네임입니다."
            )
        );
    }
}