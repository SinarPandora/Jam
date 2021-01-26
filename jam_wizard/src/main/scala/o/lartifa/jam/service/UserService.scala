package o.lartifa.jam.service

import o.lartifa.jam.model.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

/**
 * 用户服务
 *
 * Author: sinar
 * 2021/1/25 23:22
 */
@Service
class UserService extends UserDetailsService {
  override def loadUserByUsername(username: String): User = ???

  /**
   * 获取当前用户名
   *
   * @return 当前用户名
   */
  def getSessionUsername: String = SecurityContextHolder.getContext.getAuthentication.getName
}
