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

// 할인중독 슬레이브 디비 연결

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "thirdSlaveEntityManager", transactionManagerRef = "thirdSlaveTransactionManager", basePackages = "com.socket.auction.repository.third.slave")
public class ThirdSlaveDBConfig {
	
	@Autowired
	private Environment env;

	@Bean
	public DataSource thirdSlaveDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName(env.getProperty("spring.thirdSlave.datasource.driverClassName"));
		dataSource.setUrl(env.getProperty("spring.thirdSlave.datasource.url"));
		dataSource.setUsername(env.getProperty("spring.thirdSlave.datasource.username"));
		dataSource.setPassword(env.getProperty("spring.thirdSlave.datasource.password"));

		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean thirdSlaveEntityManager() {
		HashMap<String, Object>                properties                             = new HashMap<>();
		HibernateJpaVendorAdapter              vendorAdapter                          = new HibernateJpaVendorAdapter();
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

		localContainerEntityManagerFactoryBean.setDataSource(thirdSlaveDataSource());
		localContainerEntityManagerFactoryBean.setPackagesToScan("com.socket.auction.entity");
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("thirdSlaveEntityManager");
		localContainerEntityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);		

		properties.put("hibernate.hbm2ddl.auto",env.getProperty("hibernate.hbm2ddl.auto"));
		properties.put("hibernate.dialect",env.getProperty("hibernate.dialect"));	
		localContainerEntityManagerFactoryBean.setJpaPropertyMap(properties);

		return localContainerEntityManagerFactoryBean;
	}

	@Bean
	public PlatformTransactionManager thirdSlaveTransactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(thirdSlaveEntityManager().getObject());

		return transactionManager;
	}
}