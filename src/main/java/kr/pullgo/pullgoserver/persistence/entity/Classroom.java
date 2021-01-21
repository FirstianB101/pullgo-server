package kr.pullgo.pullgoserver.persistence.entity;

import com.sun.istack.NotNull;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Entity
@Builder
@AllArgsConstructor
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Teacher> teachers = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Student> students = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "classrooms")
    private Set<Student> applyingStudents = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "classrooms")
    private Set<Teacher> applyingTeachers = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Lesson> lessons = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "classroom")
    private Set<Exam> exams = new HashSet<>();
}
