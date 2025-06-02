package team.budderz.buddyspace.domain.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.api.user.request.SignupRequest;
import team.budderz.buddyspace.api.user.response.SignupResponse;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.global.exception.GlobalExceptionHandler;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        if(userRepository.findByEmail(signupRequest.email()).isPresent()) {
            throw new UserException(UserErrorCode.INVALID_USER_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.password());

        User user = new User(
                signupRequest.name(),
                signupRequest.email(),
                encodedPassword,
                signupRequest.birthDate(),
                signupRequest.gender(),
                signupRequest.address(),
                signupRequest.phone(),
                signupRequest.provider(),
                signupRequest.role()
        );

        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }
}
