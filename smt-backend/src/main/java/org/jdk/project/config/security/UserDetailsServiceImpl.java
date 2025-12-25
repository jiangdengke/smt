package org.jdk.project.config.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jdk.project.repository.UserRepository;
import org.jooq.generated.tables.pojos.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** 基于用户ID加载用户与权限信息的服务实现。 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * 根据用户ID加载用户详情（包含角色权限）。
   *
   * @param id 用户ID（字符串形式）
   * @return Spring Security UserDetails
   */
  @Override
  public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
    User dbUser = userRepository.fetchOneById(Long.valueOf(id));
    if (dbUser == null) {
      throw new UsernameNotFoundException(String.format("uid %s user not found", id));
    }
    Long userId = toLongId(dbUser.getId());
    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
    List<String> roleCodes = userRepository.fetchRoleCodesByUserId(userId);
    for (String roleCode : roleCodes) {
      if (roleCode == null || roleCode.isBlank()) {
        continue;
      }
      String normalized = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
      authorities.add(new SimpleGrantedAuthority(normalized));
    }
    List<String> permissionCodes = userRepository.fetchPermissionCodesByUserId(userId);
    for (String permissionCode : permissionCodes) {
      if (permissionCode == null || permissionCode.isBlank()) {
        continue;
      }
      authorities.add(new SimpleGrantedAuthority(permissionCode));
    }

    return new org.springframework.security.core.userdetails.User(
        dbUser.getUsername(), dbUser.getPassword(), true, true, true, true, authorities);
  }

  private Long toLongId(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Long longValue) {
      return longValue;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    String text = value.toString();
    if (text.isBlank()) {
      return null;
    }
    return Long.valueOf(text);
  }
}
