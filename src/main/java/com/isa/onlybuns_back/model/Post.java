package com.isa.onlybuns_back.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Post {
    @Id
    @SequenceGenerator(
            name = "post_sequence",
            sequenceName = "post_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "post_sequence"
    )
    @Column(
            name = "post_id",
            updatable = false
    )
    private long id;

    @Column(
            name = "description",
            nullable = false
    )
    private String description;

    @ElementCollection
    private List<String> imagePaths;

    @ManyToOne
    private User user;
}
