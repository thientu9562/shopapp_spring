package com.example.shopapp.services.users;

import com.example.shopapp.components.JwrTokenUtil;
import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Role;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.RoleRepository;
import com.example.shopapp.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService{
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwrTokenUtil jwrTokenUtil;

    private final AuthenticationManager authenticationManager;
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwrTokenUtil jwrTokenUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwrTokenUtil = jwrTokenUtil;
        this.authenticationManager = authenticationManager;
    }
    @Override
    public User createUser(UserDTO userDTO) throws DataNotFoundException {

        String phoneNumber = userDTO.getPhoneNumber();

        // Check exist
        if(userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exist");
        }

        // Convert from UserDTO => User
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber((userDTO.getPhoneNumber()))
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .build();

        // Check role
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        newUser.setRole(role);

        // check account id -> not require password
         if(userDTO.getFacebookAccountId() ==0 && userDTO.getGoogleAccountId() == 0) {
             String password = userDTO.getPassword();

             // encode password
             String encodePassword = passwordEncoder.encode(password);
             newUser.setPassword(encodePassword);
         }
        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password) throws Exception {
        Optional<User> optUser = userRepository.findByPhoneNumber(phoneNumber);
        if(optUser.isEmpty()) {
            throw new DataNotFoundException("Invalid phoneNumber/password");
        }
        User user = optUser.get();

        // Check password
        if(user.getFacebookAccountId() ==0 && user.getGoogleAccountId() == 0) {
            if(!passwordEncoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("Wrong phoneNumber or password");
            }
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        phoneNumber,
                        password,
                        user.getAuthorities()
                );


        // Authenticate with java security
        authenticationManager.authenticate(authenticationToken);
       return jwrTokenUtil.generateToken(user);
    }
}
