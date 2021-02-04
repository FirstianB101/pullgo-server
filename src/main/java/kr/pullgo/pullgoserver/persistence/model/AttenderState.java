package kr.pullgo.pullgoserver.persistence.model;

import com.sun.istack.NotNull;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Entity
public class AttenderState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @NotNull
    @ManyToOne
    private Student attender;

    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @NotNull
    @ManyToOne
    private Exam exam;

    @Enumerated(EnumType.STRING)
    @NotNull
    private AttendingProgress progress = AttendingProgress.BEFORE_EXAM;

    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "attenderState", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<AttenderAnswer> answers = new HashSet<>();

    private Integer score = null;

    public void setAttender(Student attender) {
        this.attender = attender;
        attender.getAttendingStates().add(this);
    }

    public void setExam(Exam exam) {
        this.exam = exam;
        exam.getAttenderStates().add(this);
    }
}
