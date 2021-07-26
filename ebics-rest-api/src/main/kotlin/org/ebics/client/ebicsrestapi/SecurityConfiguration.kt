package org.ebics.client.ebicsrestapi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder


@Configuration
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
class SecurityConfiguration() : WebSecurityConfigurerAdapter(true) {

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
            .withUser("user").password("{noop}pass")
            .roles("USER")
            .and()
            .withUser("admin").password("{noop}pass")
            .roles("ADMIN", "USER")
    }

    override fun configure(http: HttpSecurity) {
        //http.httpBasic().and().authorizeRequests().antMatchers("/users", "/").permitAll().anyRequest().authenticated()
        http
            .httpBasic()
            .and()
            .authorizeRequests()
            //.antMatchers(HttpMethod.GET, "/users").hasRole("USER")
            //.antMatchers(HttpMethod.GET, "/users/**").hasRole("USER")
            .antMatchers(HttpMethod.POST, "/users").hasRole("USER")
            .antMatchers(HttpMethod.PUT, "/users").hasRole("USER")
            .antMatchers(HttpMethod.DELETE, "/users").hasRole("USER")
            //.antMatchers(HttpMethod.GET, "/banks/**").hasRole("USER")
            .antMatchers(HttpMethod.POST, "/banks/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.PUT, "/banks/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.PATCH, "/banks/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.DELETE, "/banks/**").hasRole("ADMIN")
            .and()
            .cors().and()
            .csrf().disable()
            .formLogin().disable();

    }
}
