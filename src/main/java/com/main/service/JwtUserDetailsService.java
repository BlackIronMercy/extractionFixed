package com.main.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.main.common.GlobalConstant;
import com.main.model.User;
import com.main.repo.UserRepo;



@Service
public class JwtUserDetailsService implements UserDetailsService {


    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    	List<String> statusList=new ArrayList<>(); 
    	statusList.add(GlobalConstant.ACTIVE_STATUS);
    	statusList.add(GlobalConstant.PASSWORD_CHANGE_STATUS);
        User user = userRepo.findByUsernameAndStatusIn(username,statusList);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                new ArrayList<>());
    }

    
}
