package com.example.user.service;

import com.example.user.entity.User;
import com.example.user.exception.UnauthorizedException;
import com.example.user.exception.UserNotFoundException;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonMethodsService {
    private final UserRepository userRepository;

    private void checkIfUserIdTheSameWithIdOfUserFromToken(Integer userId, Integer id) {
        if (!userId.equals(id)){
            throw new UnauthorizedException("Unauthorized access");
        }
    }
    public User getUserIfItIsAuthorizedAccess(Integer userId, UserDetails userDetails){
        User user = userRepository.findById(Integer.valueOf(userDetails.getUsername()))
                .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));

        log.debug("Checking ids");
        checkIfUserIdTheSameWithIdOfUserFromToken(userId, user.getId());
        log.debug("Ids do match");

        return user;
    }
}
