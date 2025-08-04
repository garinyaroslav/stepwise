
package com.github.stepwise.security;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {

  private final User user;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  public Long getId() {
    return user.getId();
  }

  public UserRole getRole() {
    return user.getRole();
  }

}
