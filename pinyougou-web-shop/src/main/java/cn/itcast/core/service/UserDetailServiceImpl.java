package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

/**
 * 自定义认证类
 */
public class UserDetailServiceImpl implements UserDetailsService {

    private SellerService sellerService;
    //不使用注解 需要提供set方法传过去
    public void setSellerService (SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername (String username) throws UsernameNotFoundException {
        //入参:用户名
        //返回值 商家对象
        Seller seller = sellerService.findOne(username);
        //判断用户名是否存在
        if ("1".equals (seller.getStatus ())){
            Set<GrantedAuthority> authorities = new HashSet <> ();
            authorities.add (new SimpleGrantedAuthority ("ROLE_SELLER"));
            return new User (username,seller.getPassword (),authorities);
        }

        return null;
    }
}
