package com.ssm.chapter18.config;

import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan("com.*")
@EnableTransactionManagement
public class RootConfig implements TransactionManagementConfigurer {
    private DataSource dataSource;

    @Override
    @Bean(name = "annotationDrivenTransactionManager")
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(initDataSource());
        return transactionManager;
    }

    /**
     * 配置數據庫
     *
     * @return 數據連接池
     */
    @Bean(name = "dataSource")
    public DataSource initDataSource() {
        if (dataSource != null) {
            return dataSource;
        }

        Properties props = new Properties();
        props.setProperty("driverClassName", "com.mysql.jdbc.Driver");
        props.setProperty("url", "jdbc:mysql://localhost:3306/chapter6");
        props.setProperty("username", "root");
        props.setProperty("password", "root");
        try {
            dataSource = BasicDataSourceFactory.createDataSource(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSource;
    }

    /**
     * 配置 SqlSessionFactoryBean
     *
     * @return SqlSessionFactoryBean
     */
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactoryBean initSessionFactory() {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(initDataSource());

        // 配置 MyBatis 配置文件
        Resource resource = new ClassPathResource("RoleMapper.xml");
        sqlSessionFactory.setConfigLocation(resource);
        return sqlSessionFactory;
    }

    /**
     * Mapper 掃描儀配置
     */
    @Bean
    public MapperScannerConfigurer initMapperScannerConfigurer() {
        MapperScannerConfigurer msc = new MapperScannerConfigurer();
        msc.setBasePackage("com.*");
        msc.setSqlSessionFactoryBeanName("sqlSessionFactory");

        // 有以下類型註解的才會被當作 mapper 類
        msc.setAnnotationClass(Repository.class);
        return msc;
    }

}
