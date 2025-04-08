package com.socket.auction.config.db;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// 공구마켓 마스터 디비 연결

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "firstMasterEntityManager", transactionManagerRef = "firstMasterTransactionManager", basePackages = "com.socket.auction.repository.first.master")
public class FirstMasterDBConfig {
    
    @Autowired
    private Environment env;
    
    @Primary
    @Bean
    public DataSource firstMasterDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(env.getProperty("spring.datasource.driverClassName"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));

        return dataSource;
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean firstMasterEntityManager() {
		HashMap<String, Object>                properties                             = new HashMap<>();
		HibernateJpaVendorAdapter              vendorAdapter                          = new HibernateJpaVendorAdapter();
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        localContainerEntityManagerFactoryBean.setDataSource(firstMasterDataSource());
        localContainerEntityManagerFactoryBean.setPackagesToScan(new String[] { "com.socket.auction.entity" });
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("firstMasterEntityManager");
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);       
        
        
		properties.put("hibernate.hbm2ddl.auto",env.getProperty("hibernate.hbm2ddl.auto"));
		properties.put("hibernate.dialect",env.getProperty("hibernate.dialect"));
        localContainerEntityManagerFactoryBean.setJpaPropertyMap(properties);

        return localContainerEntityManagerFactoryBean;
    }

    @Primary
    @Bean
    public PlatformTransactionManager firstMasterTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(firstMasterEntityManager().getObject());

        return transactionManager;
    }
}