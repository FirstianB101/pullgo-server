package kr.pullgo.pullgoserver.persistence.model;

import com.sun.istack.NotNull;
import java.time.LocalDateTime;
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
import kr.pullgo.pullgoserver.error.exception.AttenderAnswerNotFoundException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Getter
@Setter
@With
@ToString
@Entity
public class AttenderState extends TimeEntity {

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
    private AttendingProgress progress = AttendingProgress.ONGOING;

    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "attenderState", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AttenderAnswer> answers = new HashSet<>();

    private Integer score = null;

    @Setter(AccessLevel.NONE)
    @NotNull
    private LocalDateTime examStartTime;

    public boolean isOutOfTimeRange(LocalDateTime now) {
        return now.isBefore(exam.getBeginDateTime())
            || now.isAfter(exam.getEndDateTime());
    }

    public boolean isAfterTimeLimit(LocalDateTime now) {
        LocalDateTime deadLine = examStartTime.plus(exam.getTimeLimit());
        return now.isAfter(deadLine);
    }

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
        if (!answers.contains(attenderAnswer)) {
            throw new AttenderAnswerNotFoundException();
        }

        this.answers.remove(attenderAnswer);
        attenderAnswer.setAttenderState(null);
    }

    @Builder
    public AttenderState(LocalDateTime examStartTime) {
        this.examStartTime = examStartTime;
    }
}
