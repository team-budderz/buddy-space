package team.budderz.buddyspace.global.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import team.budderz.buddyspace.infra.database.user.entity.UserRole;

import java.util.Collection;
import java.util.List;

@Getter
public class UserAuth implements UserDetails {

    private final Long userId;
    private final String userName;
    private final String email;
    private final String password;
    private final String phone;
    private final UserRole role;

    public UserAuth(Long userId, String userName, String email, String password, String phone, UserRole role) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true;}
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
