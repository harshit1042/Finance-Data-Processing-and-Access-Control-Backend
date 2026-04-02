package com.assessment.finance.model;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="users")
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false, unique=true, length=100) private String username;
 @Column(name="password_hash", nullable=false, length=255) private String passwordHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private Role role;
 @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private UserStatus status;
 @Column(nullable=false, updatable=false) private Instant createdAt;
 @PrePersist void onCreate(){createdAt=Instant.now();}
 public Long getId(){return id;} public String getUsername(){return username;} public void setUsername(String v){username=v;}
 public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
 public Role getRole(){return role;} public void setRole(Role v){role=v;} public UserStatus getStatus(){return status;} public void setStatus(UserStatus v){status=v;}
 public Instant getCreatedAt(){return createdAt;}
}
