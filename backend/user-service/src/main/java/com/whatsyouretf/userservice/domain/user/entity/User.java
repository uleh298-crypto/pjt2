package com.whatsyouretf.userservice.domain.user.entity;

import com.whatsyouretf.userservice.common.entity.BaseEntity;
import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"user\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(unique = true, length = 50)
    private String nickname;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_my_data_accepted")
    @Builder.Default
    private Boolean isMyDataAccepted = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserSocialAccount> socialAccounts = new ArrayList<>();

    public void acceptMyData() {
        if (isMyDataAccepted) {
            throw new BusinessException(ErrorCode.ALREADY_ACCEPTED_MYDATA);
        }

        isMyDataAccepted = true;
    }

    public void checkMyDataAccepted() {
        if(!isMyDataAccepted) {
            throw new BusinessException(ErrorCode.MYDATA_NOT_CONNECTED);
        }
    }

    public enum Role {
        USER, ADMIN
    }

    // 비즈니스 메서드
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateProfile(String nickname, String profileImage) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    public void clearProfileImage() {
        this.profileImage = null;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void addSocialAccount(UserSocialAccount socialAccount) {
        this.socialAccounts.add(socialAccount);
        socialAccount.setUser(this);
    }

    public static User of(Long userId) {
        User user = new User();
        user.id = userId;
        return user;
    }
}
