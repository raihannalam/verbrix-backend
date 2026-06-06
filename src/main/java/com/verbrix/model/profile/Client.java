package com.verbrix.model.profile;

import com.verbrix.model.rbac.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;


    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id",  unique = true, nullable = false)
    private User user;


}
