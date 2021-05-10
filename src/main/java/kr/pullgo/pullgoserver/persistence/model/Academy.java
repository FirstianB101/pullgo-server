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
import kr.pullgo.pullgoserver.error.exception.ClassroomNotFoundException;
import kr.pullgo.pullgoserver.error.exception.StudentNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherNotFoundException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@With
@ToString
@Entity
public class Academy extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String phone;

    @NotNull
    private String address;

    @ToString.Exclude
    @Setter(AccessLevel.NONE)
    @NotNull
    @ManyToOne
    private Teacher owner;

    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Student> students = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Teacher> teachers = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "appliedAcademies")
    private Set<Student> applyingStudents = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "appliedAcademies")
    private Set<Teacher> applyingTeachers = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "academy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Classroom> classrooms = new HashSet<>();

    @Builder
    public Academy(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public void setOwner(Teacher teacher) {
        if (!teachers.contains(teacher))
            throw new TeacherNotFoundException();

        this.owner = teacher;
    }

    public void acceptStudent(Student student) {
        if (!applyingStudents.contains(student)) {
            throw new StudentNotFoundException();
        }

        addStudent(student);
        student.removeAppliedAcademy(this);
    }

    private void addStudent(Student student) {
        students.add(student);
        student.getAcademies().add(this);
    }

    public void removeStudent(Student student) {
        if (!students.contains(student)) { throw new StudentNotFoundException(); }

        students.remove(student);
        student.getAcademies().remove(this);
    }

    public void acceptTeacher(Teacher teacher) {
        if (!applyingTeachers.contains(teacher)) { throw new TeacherNotFoundException(); }

        addTeacher(teacher);
        teacher.removeAppliedAcademy(this);
    }

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
        teacher.getAcademies().add(this);
    }

    public void removeTeacher(Teacher teacher) {
        if (!teachers.contains(teacher)) { throw new TeacherNotFoundException(); }

        teachers.remove(teacher);
        teacher.getAcademies().remove(this);
    }

    public void addClassroom(Classroom classroom) {
        classrooms.add(classroom);
        classroom.setAcademy(this);
    }

    public void removeClassroom(Classroom classroom) {
        if (!classrooms.contains(classroom)) { throw new ClassroomNotFoundException(); }

        classrooms.remove(classroom);
    }
}
