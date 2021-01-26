package o.lartifa.jam.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

import java.util
import scala.jdk.CollectionConverters._

/**
 * 用户
 *
 * Author: sinar
 * 2021/1/25 23:30
 */
case class User
(
  username: String,
  password: String,
  isAccountNonExpired: Boolean,
  isAccountNonLocked: Boolean,
  isCredentialsNonExpired: Boolean,
  isEnabled: Boolean,
  authorities: List[GrantedAuthority]
) extends UserDetails {
  override def getAuthorities: util.Collection[_ <: GrantedAuthority] = this.authorities.asJava

  override def getPassword: String = this.password

  override def getUsername: String = this.username
}
