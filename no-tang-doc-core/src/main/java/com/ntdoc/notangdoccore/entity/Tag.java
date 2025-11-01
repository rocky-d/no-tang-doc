package com.ntdoc.notangdoccore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(
        name = "tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tags_tag",columnNames = "tag")
        }
)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="tag",nullable = false,length = 64)
    private String tag;

    @ManyToMany
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Document> documents =  new HashSet<>();
}
