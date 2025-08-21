package com.fittura.domain.member.entity;

import com.fittura.domain.member.constant.Role;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class Member extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 20)
    private String nickName;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    @Builder
    private Member(String email, String name, String nickName, String password, Set<Role> roles) {
        this.email = email;
        this.name = name;
        this.nickName = nickName;
        this.password = password;
        this.roles = roles;
    }

    public static Member createUser(String email, String name, String nickName, String password) {
        return Member.builder()
                .email(email)
                .name(name)
                .nickName(nickName)
                .password(password)
                .roles(Set.of(Role.USER))
                .build();
    }

    public static Member createAdmin(String email, String name, String nickName, String password) {
        return Member.builder()
                .email(email)
                .name(name)
                .nickName(nickName)
                .password(password)
                .roles(Set.of(Role.ADMIN))
                .build();
    }
}
