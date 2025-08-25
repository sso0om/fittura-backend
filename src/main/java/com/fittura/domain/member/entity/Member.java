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

    @Column(unique = true, nullable = false, length = 254)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 60)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    @Builder
    private Member(String email, String name, String nickname, String password, Set<Role> roles) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.roles = new HashSet<>(roles);
    }

    public static Member createUser(String email, String name, String nickname, String password) {
        return Member.builder()
                .email(email)
                .name(name)
                .nickname(nickname)
                .password(password)
                .roles(Set.of(Role.ROLE_USER))
                .build();
    }

    public static Member createAdmin(String email, String name, String nickname, String password) {
        return Member.builder()
                .email(email)
                .name(name)
                .nickname(nickname)
                .password(password)
                .roles(Set.of(Role.ROLE_ADMIN))
                .build();
    }

    public void grantRole(Role role) {
        if(role == null) return;
        this.roles.add(role);
    }

    public void revokeRole(Role role) {
        if(role == null) return;
        this.roles.remove(role);
    }

    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }
}
