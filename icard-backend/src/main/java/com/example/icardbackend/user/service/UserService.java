package com.example.icardbackend.user.service;

import com.example.icardbackend.security.jwt.JwtService;
import com.example.icardbackend.security.userdetails.CustomUserDetails;
import com.example.icardbackend.user.dto.AuthResponse;
import com.example.icardbackend.user.dto.LoginRequest;
import com.example.icardbackend.user.dto.RegisterRequest;
import com.example.icardbackend.user.dto.UserResponse;
import com.example.icardbackend.user.entity.User;
import com.example.icardbackend.user.mapper.UserMapper;
import com.example.icardbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.icardbackend.common.exception.UserException.EmailAlreadyExistsException;
import com.example.icardbackend.common.exception.UserException.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) { //dbden bu maile ait kullanıcı var mı diye bakıyor repo aracılığıyla
            throw new EmailAlreadyExistsException("Bu email adresi zaten kayıtlı: " + request.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = userMapper.toEntity(request, encodedPassword); //builder
        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser); //direkt entity'yi dönmek yerine UserResponse döndük
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        // yanlış şifre/mail girilirse buradan exception fırlatılır
        // GlobalExceptionHandler bunu otomatik yakalayıp 401 dönecek

        User user = userRepository.findByEmail(request.getEmail()) //dbden bu maile ait kullanıcıyı buluyor bakıyor repo aracılığıyla
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + request.getEmail()));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toResponse(user))
                .build();
    }
}