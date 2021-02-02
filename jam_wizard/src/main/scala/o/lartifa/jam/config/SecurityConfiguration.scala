package o.lartifa.jam.config

import o.lartifa.jam.service.UserService
import org.springframework.context.annotation.{Configuration, Lazy}
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

/**
 * 安全设置
 *
 * Author: sinar
 * 2021/1/25 23:18
 */
@Configuration
@EnableWebSecurity
class SecurityConfiguration(@Lazy userService: UserService) extends WebSecurityConfigurerAdapter {
  override def configure(auth: AuthenticationManagerBuilder): Unit = {
    auth
      .userDetailsService(userService)
      .passwordEncoder(new BCryptPasswordEncoder())
  }

  override def configure(http: HttpSecurity): Unit = {
    http
      .authorizeRequests()
      .antMatchers("/system/*").permitAll()
      // .anyRequest().authenticated()
  }
}
