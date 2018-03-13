package com.tweeter.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Configures spring security. I'm not explaining this, it's too complicated. GRTM.
 */
@Configuration
@EnableWebSecurity
@EnableScheduling
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class TwitterSecurityConfig extends WebSecurityConfigurerAdapter {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/js/**", "/css/**", "/images/**", "/font/**", "/register/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(((EntityManagerFactoryInfo) entityManager.getEntityManagerFactory()).getDataSource())
                .rolePrefix("ROLE_")
                .passwordEncoder(passwordEncoder)
                .usersByUsernameQuery("SELECT USERNAME,PASSWORD,ENABLED FROM USERS WHERE USERNAME=?")
                .authoritiesByUsernameQuery("SELECT USERNAME,ROLE FROM USER_ROLES WHERE USERNAME=?");
    }
}
