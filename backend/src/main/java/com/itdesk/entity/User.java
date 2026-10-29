package com.itdesk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

//import java.util.Collection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {
    @Column(name="email",nullable=false,unique=true,length=150)
    private String email;
    @Column(name="password_hash",nullable=false,length=255)
    private String passwordHash;
    @Column(name="first_name",nullable=false,length=50)
    private String firstName;
    @Column(name="last_name",length=50)
    private String lastName;
    @Column(name="job_title",length=100)
    private String jobTitle;
    @Column(name="phone",length=20)
    private String phone;
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="department_id")
    private Department department;
    @Builder.Default
    @ManyToMany(fetch=FetchType.EAGER
    )
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles=new HashSet<>();

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public String getFullName()
    {
        return (firstName + " " +(lastName!=null ? lastName: "")).trim();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return roles.stream()
                .map(role-> new SimpleGrantedAuthority(role.getName().name())).collect(Collectors.toSet());
    }
    @Override
    public String getPassword()
    {
        return passwordHash;
    }
    @Override
    public String getUsername()
    {
        return email;
    }
    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }
    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    public boolean isCredentialsNonExpired()
    {
        return true;
    }
    public boolean isEnabled()
    {
        return isActive && !isDeleted();
    }


}
