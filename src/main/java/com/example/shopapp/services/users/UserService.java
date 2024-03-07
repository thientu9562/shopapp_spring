package com.example.shopapp.services.users;

import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Role;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.RoleRepository;
import com.example.shopapp.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService{
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .build();

        Role role = roleRepository.findById(userDTO.getRoleId()).orElseThrow(()-> new DataNotFoundException("Role not found"));
        newUser.setRole(role);
        // check account id -> not require password
         if(userDTO.getFacebookAccountId() ==0 && userDTO.getGoogleAccountId() == 0) {
             String password = userDTO.getPassword();
             newUser.setPassword(password);
         }
        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password) {

        return null;
    }
}
