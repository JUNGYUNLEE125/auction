package com.socket.auction.repository.third.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.UserEntity;

@Repository("third.slave.UserThrdRepository")
public interface UserThrdRepository extends JpaRepository<UserEntity, Long>, UserThrdRepositoryCustom {
    public UserEntity findByMmbrId(String mmbrId);
}
