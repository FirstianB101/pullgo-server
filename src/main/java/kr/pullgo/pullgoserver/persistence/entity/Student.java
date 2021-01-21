package kr.pullgo.pullgoserver.persistence.entity;

import com.sun.istack.NotNull;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne
    @ToString.Exclude
    private Account account;

    @NotNull
    private String parentPhone;

    @NotNull
    private String schoolName;

    private int schoolYear;

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "students")
    private Set<Academy> academies = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @ManyToMany(mappedBy = "students")
    private Set<Classroom> classrooms = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Academy> appliedAcademies = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @ManyToMany
    private Set<Classroom> appliedClassrooms = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "attender")
    private Set<AttenderState> attendingStates = new HashSet<>();
}

