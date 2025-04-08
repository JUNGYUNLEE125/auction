package com.socket.auction.repository.second.slave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socket.auction.entity.UserEntity;

@Repository("second.slave.UserScndRepository")
public interface UserScndRepository extends JpaRepository<UserEntity, Long>, UserScndRepositoryCustom {
    public UserEntity findByMmbrId(String mmbrId);
}
