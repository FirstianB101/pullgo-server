package kr.pullgo.pullgoserver.persistence.model;

import com.sun.istack.NotNull;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne
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

    public void removeAppliedAcademy(Academy academy){
        appliedAcademies.remove(academy);
        academy.getApplyingTeachers().remove(this);
    }

    public void applyAcademy(Academy academy){
        this.appliedAcademies.add(academy);
        academy.getApplyingTeachers().add(this);
    }

    public void removeAppliedClassroom(Classroom classroom){
        this.appliedClassrooms.remove(classroom);
        classroom.getApplyingTeachers().remove(this);
    }

    public void applyClassroom(Classroom classroom){
        this.appliedClassrooms.add(classroom);
        classroom.getApplyingTeachers().add(this);
    }
}
