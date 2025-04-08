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

// 할인중독 로그 디비 연결

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "thirdLogEntityManager", transactionManagerRef = "thirdLogTransactionManager", basePackages = "com.socket.auction.repository.third.log")
public class ThirdLogDBConfig {

	@Autowired
	private Environment env;

	@Bean
	public DataSource thirdLogDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();

		dataSource.setDriverClassName(env.getProperty("spring.thirdLog.datasource.driverClassName"));
		dataSource.setUrl(env.getProperty("spring.thirdLog.datasource.url"));
		dataSource.setUsername(env.getProperty("spring.thirdLog.datasource.username"));
		dataSource.setPassword(env.getProperty("spring.thirdLog.datasource.password"));

		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean thirdLogEntityManager() {
		HashMap<String, Object> properties = new HashMap<>();
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

		localContainerEntityManagerFactoryBean.setDataSource(thirdLogDataSource());
		localContainerEntityManagerFactoryBean.setPackagesToScan(new String[] { "com.socket.auction.entity.log" });
		localContainerEntityManagerFactoryBean.setPersistenceUnitName("thirdLogEntityManager");
		localContainerEntityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);

		properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
		properties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
		localContainerEntityManagerFactoryBean.setJpaPropertyMap(properties);

		return localContainerEntityManagerFactoryBean;
	}

	@Bean
	public PlatformTransactionManager thirdLogTransactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(thirdLogEntityManager().getObject());

		return transactionManager;
	}
}