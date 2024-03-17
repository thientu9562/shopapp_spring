package com.example.shopapp.controllers;

import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.dtos.UserLoginDTO;
import com.example.shopapp.models.User;
import com.example.shopapp.services.users.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {

     private final UserService userService;
     public UserController(UserService userService) {
         this.userService = userService;
     }
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO,
                                        BindingResult result) {
        try {
            if(result.hasErrors()){
                List<String> errorMessage = result.getFieldErrors()
                        .stream().map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }

            // check confirm password
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                 return ResponseEntity.badRequest().body("Password does not match");
            }
            User user = userService.createUser(userDTO);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        try {
            String token = userService.login(userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
