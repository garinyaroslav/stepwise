package com.github.stepwise.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "study_group")
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "group_student", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "student_id"))
    private List<User> students = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<AcademicWork> academicWorks = new ArrayList<>();

    public StudyGroup(String name, List<User> students) {
        this.name = name;
        this.students = students;
    }

    public StudyGroup(Long id, List<User> students) {
        this.id = id;
        this.students = students;
    }

}
