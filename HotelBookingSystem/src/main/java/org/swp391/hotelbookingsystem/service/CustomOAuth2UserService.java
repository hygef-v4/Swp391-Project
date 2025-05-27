package org.swp391.hotelbookingsystem.service;

import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

//    @Autowired
//    private UserRepo userRepo;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//
//        System.out.println("OAuth2 attributes: " + oAuth2User.getAttributes());
//        String email = oAuth2User.getAttribute("email");
//        System.out.println("Email from OAuth2: " + email);
//
//        User user = userRepo.findByEmail(email);
//        if(user != null) {
//            return oAuth2User;
//        }else{
//            user = new User();
//            user.setEmail(email);
//            userRepo.saveUserFromGoogle(user);
//            return oAuth2User;
//        }
//    }
}
