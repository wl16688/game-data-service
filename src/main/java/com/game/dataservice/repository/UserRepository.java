package com.game.dataservice.repository;

import com.game.dataservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOpenid(String openid);

    Page<User> findByNicknameContainingOrOpenidContaining(String nickname, String openid, Pageable pageable);
}
