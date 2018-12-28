package cn.itcast.core.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

public class UserDetailServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername (String username) throws UsernameNotFoundException {
        Set<GrantedAuthority> authorities = new HashSet<> ();
        authorities.add (new SimpleGrantedAuthority ("ROLE_USER"));
        return new User (username, "", authorities);
    }
}
