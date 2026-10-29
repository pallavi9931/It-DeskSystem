package com.itdesk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name="departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity{
    @Column(name="name",nullable=false, length=100)
    private String name;
    @Column(name="code" ,nullable=false, unique=true , length=20)
    private String code;
    @Column(name="manager_id")
    private Long managerId;



}
