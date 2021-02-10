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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
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
    @OneToMany(mappedBy = "attenderState", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public void addAnswer(AttenderAnswer attenderAnswer) {
        this.answers.add(attenderAnswer);
        attenderAnswer.setAttenderState(this);
    }

    public void removeAnswer(AttenderAnswer attenderAnswer) {
        this.answers.remove(attenderAnswer);
        attenderAnswer.setAttenderState(null);
    }
}
