package kr.pullgo.pullgoserver.persistence.model;

import com.sun.istack.NotNull;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Entity
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Teacher> teachers = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Student> students = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "appliedClassrooms")
    private Set<Student> applyingStudents = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "appliedClassrooms")
    private Set<Teacher> applyingTeachers = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Lesson> lessons = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "classroom")
    private Set<Exam> exams = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToOne
    private Academy academy;

    @Builder
    public Classroom(String name) {
        this.name = name;
    }
}
