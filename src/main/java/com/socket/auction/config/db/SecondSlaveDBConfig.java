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

// 심쿵할인 슬레이브 디비 연결

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "secondSlaveEntityManager", transactionManagerRef = "secondSlaveTransactionManager", basePackages = "com.socket.auction.repository.second.slave")
public class SecondSlaveDBConfig {
	
	@Autowired
	private Environment env;

	@Bean
	public DataSource secondSlaveDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName(env.getProperty("spring.secondSlave.datasource.driverClassName"));
		dataSource.setUrl(env.getProperty("spring.secondSlave.datasource.url"));
		dataSource.setUsername(env.getProperty("spring.secondSlave.datasource.username"));
		dataSource.setPassword(env.getProperty("spring.secondSlave.datasource.password"));

		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean secondSlaveEntityManager() {
		HashMap<String, Object>                properties                             = new HashMap<>();
		HibernateJpaVendorAdapter              vendorAdapter                          = new HibernateJpaVendorAdapter();
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

		localContainerEntityManagerFactoryBean.setDataSource(secondSlaveDataSource());
		localContainerEntityManagerFactoryBean.setPackagesToScan("com.socket.auction.entity");
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("secondSlaveEntityManager");
		localContainerEntityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);		

		properties.put("hibernate.hbm2ddl.auto",env.getProperty("hibernate.hbm2ddl.auto"));
		properties.put("hibernate.dialect",env.getProperty("hibernate.dialect"));	
		localContainerEntityManagerFactoryBean.setJpaPropertyMap(properties);

		return localContainerEntityManagerFactoryBean;
	}

	@Bean
	public PlatformTransactionManager secondSlaveTransactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(secondSlaveEntityManager().getObject());

		return transactionManager;
	}
}