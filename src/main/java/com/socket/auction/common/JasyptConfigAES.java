package com.socket.auction.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

// DB Properties 암호화

@Configuration
@EnableEncryptableProperties
public class JasyptConfigAES {
	
	@Value("${custom-jasypt.path}")
	private String jasyptPath;

	@Bean("jasyptEncryptor")
	public StringEncryptor stringEncryptor() {
		String passwordKey = "";
		
		try {
            // 파일 객체 생성
            File file = new File(jasyptPath);
            // 스캐너로 파일 읽기
            Scanner scan = new Scanner(file);
            while (scan.hasNextLine()) {
            	passwordKey = scan.nextLine();
            }
			scan.close();
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        }
		
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		encryptor.setProvider(new BouncyCastleProvider());
		encryptor.setPoolSize(2);
		encryptor.setPassword(passwordKey); // 암호화 키
		encryptor.setAlgorithm("PBEWithSHA256And128BitAES-CBC-BC"); // 알고리즘
		return encryptor;
	}
}