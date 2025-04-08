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

// 공구마켓 슬레이브 디비 연결

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "firstSlaveEntityManager", transactionManagerRef = "firstSlaveTransactionManager", basePackages = "com.socket.auction.repository.first.slave")
public class FirstSlaveDBConfig {
    
    @Autowired
    private Environment env;
    
    @Bean
    public DataSource firstSlaveDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(env.getProperty("spring.firstSlave.datasource.driverClassName"));
        dataSource.setUrl(env.getProperty("spring.firstSlave.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.firstSlave.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.firstSlave.datasource.password"));

        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean firstSlaveEntityManager() {
		HashMap<String, Object>                properties                             = new HashMap<>();
		HibernateJpaVendorAdapter              vendorAdapter                          = new HibernateJpaVendorAdapter();
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        localContainerEntityManagerFactoryBean.setDataSource(firstSlaveDataSource());
        localContainerEntityManagerFactoryBean.setPackagesToScan(new String[] { "com.socket.auction.entity" });
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("firstSlaveEntityManager");
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);       
        
        
		properties.put("hibernate.hbm2ddl.auto",env.getProperty("hibernate.hbm2ddl.auto"));
		properties.put("hibernate.dialect",env.getProperty("hibernate.dialect"));
        localContainerEntityManagerFactoryBean.setJpaPropertyMap(properties);

        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager firstSlaveTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(firstSlaveEntityManager().getObject());

        return transactionManager;
    }
}