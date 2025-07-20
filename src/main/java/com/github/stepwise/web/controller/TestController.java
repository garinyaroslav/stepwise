package com.github.stepwise.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.github.stepwise.security.AppUserDetails;

@RestController
@RequestMapping("/api/test")
public class TestController {

  @GetMapping("/all")
  public String allAccess() {

    return "Public Content." + " ";
  }

  @GetMapping("/user")
  public String userAccess(@AuthenticationPrincipal UserDetails userDetails) {
    var id = ((AppUserDetails) userDetails).getId();
    return "User Content. User id: " + id;
  }

}
