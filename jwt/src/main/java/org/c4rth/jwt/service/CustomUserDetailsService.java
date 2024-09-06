package org.c4rth.jwt.service;

import lombok.AllArgsConstructor;
import org.c4rth.jwt.model.RoleEntity;
import org.c4rth.jwt.model.UserEntity;
import org.c4rth.jwt.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User not exists by Username or Email"));

        Set<GrantedAuthority> authorities = userEntity.getRoleEntities().stream()
                .map((role) -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
        List<String> roles = userEntity.getRoleEntities().stream().map(RoleEntity::getName).toList();
        return User.withUsername(userEntity.getUsername())
                .password(userEntity.getPassword())
                .roles(String.valueOf(roles))
                .authorities(authorities).build();

    }
}