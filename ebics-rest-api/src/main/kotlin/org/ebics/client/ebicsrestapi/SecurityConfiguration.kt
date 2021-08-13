package org.ebics.client.ebicsrestapi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication


@Configuration
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
class SecurityConfiguration() : WebSecurityConfigurerAdapter(true) {

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
            .withUser("guest").password("{noop}pass")
            .roles("GUEST")
            .and()
            .withUser("user").password("{noop}pass")
            .roles("USER", "GUEST")
            .and()
            .withUser("admin").password("{noop}pass")
            .roles("ADMIN", "USER", "GUEST")
    }

    override fun configure(http: HttpSecurity) {
        //http.httpBasic().and().authorizeRequests().antMatchers("/users", "/").permitAll().anyRequest().authenticated()
        http
            .httpBasic()
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/bankconnections").hasAnyRole("USER", "GUEST")
            .antMatchers(HttpMethod.POST, "/bankconnections/{\\d+}/H00{\\d+}/**").hasAnyRole("USER", "GUEST")
            .antMatchers(HttpMethod.GET, "/bankconnections/{\\d+}/H00{\\d+}/**").hasAnyRole("USER", "GUEST")
            .antMatchers(HttpMethod.POST, "/bankconnections").hasRole("USER")
            .antMatchers(HttpMethod.PUT, "/bankconnections").hasRole("USER")
            .antMatchers(HttpMethod.DELETE, "/bankconnections").hasRole("USER")
            .antMatchers(HttpMethod.POST, "/banks/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.PUT, "/banks/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.PATCH, "/banks/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.DELETE, "/banks/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.GET, "/user").permitAll()
            .and()
            .cors().and()
            .csrf().disable()
            .formLogin().disable();

    }
}
