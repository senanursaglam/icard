package com.example.icardbackend.security.userdetails;

import com.example.icardbackend.user.entity.User;
import com.example.icardbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {// bu mail adresi ile
        // bir kullanıcı var mı
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + email));

        return new CustomUserDetails(user); // eğer varsa onun kullanıcı adı, şifresi vs CustomUserDetails ile okunur
    }
}