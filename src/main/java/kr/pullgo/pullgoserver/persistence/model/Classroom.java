package kr.pullgo.pullgoserver.persistence.model;

import static javax.persistence.FetchType.LAZY;

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
import javax.validation.constraints.NotEmpty;
import kr.pullgo.pullgoserver.error.exception.ExamNotFoundException;
import kr.pullgo.pullgoserver.error.exception.LessonNotFoundException;
import kr.pullgo.pullgoserver.error.exception.StudentNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherNotFoundException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@With
@ToString
@Entity
public class Classroom extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String name;

    @ToString.Exclude
    @NotNull
    @ManyToOne(fetch = LAZY)
    private Teacher creator;

    @ToString.Exclude
    @NotEmpty
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
    @ManyToOne(fetch = LAZY)
    private Academy academy;

    @Builder
    public Classroom(String name) {
        this.name = name;
    }

    protected Classroom(Long id, String name, Teacher creator,
        Set<Teacher> teachers, Set<Student> students,
        Set<Student> applyingStudents,
        Set<Teacher> applyingTeachers,
        Set<Lesson> lessons, Set<Exam> exams, Academy academy) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.teachers = teachers;
        this.students = students;
        this.applyingStudents = applyingStudents;
        this.applyingTeachers = applyingTeachers;
        this.lessons = lessons;
        this.exams = exams;
        setAcademy(academy);
    }

    public void acceptStudent(Student student) {
        if (!applyingStudents.contains(student)) {
            throw new StudentNotFoundException();
        }

        addStudent(student);
        student.removeAppliedClassroom(this);
    }

    public void addStudent(Student student) {
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

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
        teacher.getClassrooms().add(this);
    }

    public void removeTeacher(Teacher teacher) {
        if (!teachers.contains(teacher)) {
            throw new TeacherNotFoundException();
        }

        teachers.remove(teacher);
    }

    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
        if (!this.equals(lesson.getClassroom()))
            lesson.setClassroom(this);
    }

    public void removeLesson(Lesson lesson) {
        if (!lessons.contains(lesson)) {
            throw new LessonNotFoundException();
        }

        lessons.remove(lesson);
    }

    public void addExam(Exam exam) {
        exams.add(exam);
        if (!this.equals(exam.getClassroom()))
            exam.setClassroom(this);
    }

    public void removeExam(Exam exam) {
        if (!exams.contains(exam)) {
            throw new ExamNotFoundException();
        }

        exams.remove(exam);
        exam.setClassroom(null);
    }

    public void setAcademy(Academy academy) {
        if (this.academy != academy) {
            if (this.academy != null)
                this.academy.removeClassroom(this);
            this.academy = academy;
            if (academy != null) {
                academy.addClassroom(this);
            }
        }
    }
}
