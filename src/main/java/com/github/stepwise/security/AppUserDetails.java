
package com.github.stepwise.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.github.stepwise.entity.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {

  private final User user;

  // TODO: roles implementation
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // return user.getRoles().stream().map(role -> new
    // SimpleGrantedAuthority(role.name())).toList();
    return null;
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

}
