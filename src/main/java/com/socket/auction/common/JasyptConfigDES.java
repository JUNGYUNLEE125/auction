package com.socket.auction.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

// DB Properties 암호화

@Configuration
@EnableEncryptableProperties
public class JasyptConfigDES {

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
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPassword(passwordKey);			// 암호화할 때 사용하는 키
		config.setAlgorithm("PBEWithMD5AndDES");	// 암호화 알고리즘
		config.setKeyObtentionIterations("1000");	// 암호화 키를 얻기 위해 적용된 해싱 반복 횟수를 설정
		config.setPoolSize("1");					// 생성할 암호기 풀의 크기를 설정
		config.setProviderName("SunJCE");			// 암호화 알고리즘을 요청할 보안 공급자의 이름을 설정
		config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");	// salt 생성 클래스
		config.setStringOutputType("base64");	// 인코딩 방식
		encryptor.setConfig(config);
		return encryptor;
	}
}
