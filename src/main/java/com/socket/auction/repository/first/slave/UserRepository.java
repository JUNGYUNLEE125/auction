package com.socket.auction.repository.first.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.UserEntity;

@Repository("first.slave.UserRepository")
public interface UserRepository extends JpaRepository<UserEntity, Long>, UserRepositoryCustom {
    public UserEntity findByMmbrId(String mmbrId);
}
