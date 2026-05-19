package com.whatsyouretf.userservice.domain.user.repository;

import com.whatsyouretf.userservice.domain.user.entity.UserSocialAccount;
import com.whatsyouretf.userservice.domain.user.entity.UserSocialAccount.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {

    Optional<UserSocialAccount> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    List<UserSocialAccount> findByUserId(Long userId);

    @Query("SELECT sa FROM UserSocialAccount sa JOIN FETCH sa.user WHERE sa.provider = :provider AND sa.providerUserId = :providerUserId")
    Optional<UserSocialAccount> findByProviderAndProviderUserIdWithUser(
            @Param("provider") SocialProvider provider,
            @Param("providerUserId") String providerUserId
    );

    boolean existsByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    void deleteByUserIdAndProvider(Long userId, SocialProvider provider);
}
