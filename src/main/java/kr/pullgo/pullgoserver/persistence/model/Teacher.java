package kr.pullgo.pullgoserver.persistence.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

import com.sun.istack.NotNull;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import kr.pullgo.pullgoserver.error.exception.AcademyNotFoundException;
import kr.pullgo.pullgoserver.error.exception.ClassroomNotFoundException;
import kr.pullgo.pullgoserver.error.exception.TeacherAlreadyAppliedException;
import kr.pullgo.pullgoserver.error.exception.TeacherAlreadyEnrolledException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PUBLIC) // 아직 Builder에 쓰일 인자가 없으므로, 인자 없는 생성자로 생성
@Getter
@Setter
@With
@ToString
@Entity
public class Teacher extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(cascade = ALL, fetch = LAZY)
    @ToString.Exclude
    private Account account;

    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "teachers")
    private Set<Academy> academies = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "teachers")
    private Set<Classroom> classrooms = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Academy> appliedAcademies = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Classroom> appliedClassrooms = new HashSet<>();

    public void removeAppliedAcademy(Academy academy) {
        if (!appliedAcademies.contains(academy)) {
            throw new AcademyNotFoundException();
        }

        appliedAcademies.remove(academy);
        academy.getApplyingTeachers().remove(this);
    }

    public void applyAcademy(Academy academy) {
        if (academies.contains(academy)) {
            throw new TeacherAlreadyEnrolledException();
        } else if (appliedAcademies.contains(academy)) {
            throw new TeacherAlreadyAppliedException();
        }

        this.appliedAcademies.add(academy);
        academy.getApplyingTeachers().add(this);
    }

    public void removeAppliedClassroom(Classroom classroom) {
        if (!appliedClassrooms.contains(classroom)) {
            throw new ClassroomNotFoundException();
        }

        this.appliedClassrooms.remove(classroom);
        classroom.getApplyingTeachers().remove(this);
    }

    public void applyClassroom(Classroom classroom) {
        if (classrooms.contains(classroom)) {
            throw new TeacherAlreadyEnrolledException();
        } else if (appliedClassrooms.contains(classroom)) {
            throw new TeacherAlreadyAppliedException();
        }

        this.appliedClassrooms.add(classroom);
        classroom.getApplyingTeachers().add(this);
    }
}
