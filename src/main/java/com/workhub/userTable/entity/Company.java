package com.workhub.userTable.entity;

import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "company")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Company extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_name", nullable = false, length = 50)
    private String companyName;

    @Column(name = "company_number", nullable = false, length = 20)
    private String companyNumber;

    @Column(name = "tel", nullable = false, length = 20)
    private String tel;

    @Column(name = "address", nullable = false, length = 100)
    private String address;

}
