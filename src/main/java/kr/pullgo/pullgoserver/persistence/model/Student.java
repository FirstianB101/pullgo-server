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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import kr.pullgo.pullgoserver.error.exception.AcademyNotFoundException;
import kr.pullgo.pullgoserver.error.exception.ClassroomNotFoundException;
import kr.pullgo.pullgoserver.error.exception.StudentAlreadyEnrolledException;
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
public class Student extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @ToString.Exclude
    private Account account;

    @NotNull
    private String parentPhone;

    @NotNull
    private String schoolName;

    private int schoolYear;

    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "students")
    private Set<Academy> academies = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "students")
    private Set<Classroom> classrooms = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Academy> appliedAcademies = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Classroom> appliedClassrooms = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "attender", cascade = CascadeType.REMOVE)
    private Set<AttenderState> attendingStates = new HashSet<>();

    @Builder
    public Student(String parentPhone, String schoolName, int schoolYear) {
        this.parentPhone = parentPhone;
        this.schoolName = schoolName;
        this.schoolYear = schoolYear;
    }

    public void removeAppliedAcademy(Academy academy) {
        if (!appliedAcademies.contains(academy)) { throw new AcademyNotFoundException(); }

        this.appliedAcademies.remove(academy);
        academy.getApplyingStudents().remove(this);
    }

    public void applyAcademy(Academy academy) {
        if (academies.contains(academy)) { throw new StudentAlreadyEnrolledException(); }

        this.appliedAcademies.add(academy);
        academy.getApplyingStudents().add(this);
    }

    public void removeAppliedClassroom(Classroom classroom) {
        if (!appliedClassrooms.contains(classroom)) { throw new ClassroomNotFoundException(); }

        this.appliedClassrooms.remove(classroom);
        classroom.getApplyingStudents().remove(this);
    }

    public void applyClassroom(Classroom classroom) {
        if (classrooms.contains(classroom)) { throw new StudentAlreadyEnrolledException(); }

        this.appliedClassrooms.add(classroom);
        classroom.getApplyingStudents().add(this);
    }
}

