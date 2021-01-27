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
import kr.pullgo.pullgoserver.error.exception.StudentNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherNotFoundException;
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

    public void acceptStudent(Student student) {
        if (!applyingStudents.contains(student)) {
            throw new StudentNotFoundException();
        }

        addStudent(student);
        student.removeAppliedClassroom(this);
    }

    private void addStudent(Student student) {
        students.add(student);
        student.getClassrooms().add(this);
    }

    public void removeStudent(Student student) {
        if (!students.contains(student)) {
            throw new StudentNotFoundException();
        }

        students.remove(student);
    }

    public void acceptTeacher(Teacher teacher) {
        if (!applyingTeachers.contains(teacher)) {
            throw new TeacherNotFoundException();
        }

        addTeacher(teacher);
        teacher.removeAppliedClassroom(this);
    }

    private void addTeacher(Teacher teacher) {
        teachers.add(teacher);
        teacher.getClassrooms().add(this);
    }

    public void removeTeacher(Teacher teacher) {
        if (!teachers.contains(teacher)) {
            throw new TeacherNotFoundException();
        }

        teachers.remove(teacher);
    }
}
