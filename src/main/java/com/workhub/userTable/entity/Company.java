package com.workhub.userTable.entity;

import com.workhub.post.entity.PostType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "company")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer companyid;

    @Column(name = "company_name", nullable = false, length = 50)
    private String companyName;

    @Column(name = "company_number", nullable = false, length = 20)
    private String companyNumber;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "tel", nullable = false, length = 20)
    private String tel;

    @Column(name = "address", nullable = false, length = 100)
    private String address;

}
