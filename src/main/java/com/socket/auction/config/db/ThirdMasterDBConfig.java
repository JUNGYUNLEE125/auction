package com.socket.auction.config.db;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// 할인중독 마스터 디비 연결

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "thirdMasterEntityManager", transactionManagerRef = "thirdMasterTransactionManager", basePackages = "com.socket.auction.repository.third.master")
public class ThirdMasterDBConfig {
	
	@Autowired
	private Environment env;

	@Bean
	public DataSource thirdMasterDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName(env.getProperty("spring.thirdMaster.datasource.driverClassName"));
		dataSource.setUrl(env.getProperty("spring.thirdMaster.datasource.url"));
		dataSource.setUsername(env.getProperty("spring.thirdMaster.datasource.username"));
		dataSource.setPassword(env.getProperty("spring.thirdMaster.datasource.password"));

		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean thirdMasterEntityManager() {
		HashMap<String, Object>                properties                             = new HashMap<>();
		HibernateJpaVendorAdapter              vendorAdapter                          = new HibernateJpaVendorAdapter();
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

		localContainerEntityManagerFactoryBean.setDataSource(thirdMasterDataSource());
		localContainerEntityManagerFactoryBean.setPackagesToScan("com.socket.auction.entity");
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("thirdMasterEntityManager");
		localContainerEntityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);		

		properties.put("hibernate.hbm2ddl.auto",env.getProperty("hibernate.hbm2ddl.auto"));
		properties.put("hibernate.dialect",env.getProperty("hibernate.dialect"));	
		localContainerEntityManagerFactoryBean.setJpaPropertyMap(properties);

		return localContainerEntityManagerFactoryBean;
	}

	@Bean
	public PlatformTransactionManager thirdMasterTransactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(thirdMasterEntityManager().getObject());

		return transactionManager;
	}
}