package kr.pullgo.pullgoserver.persistence.model;

import static javax.persistence.FetchType.LAZY;

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
    @ManyToOne(fetch = LAZY)
    private Student attender;

    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @NotNull
    @ManyToOne(fetch = LAZY)
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

    protected AttenderState(Long id, Student attender, Exam exam,
        AttendingProgress progress,
        Set<AttenderAnswer> answers, Integer score, LocalDateTime examStartTime) {
        this.id = id;
        setAttender(attender);
        setExam(exam);
        this.progress = progress;
        this.answers = answers;
        this.score = score;
        this.examStartTime = examStartTime;
    }

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
        if (attender != null)
            attender.getAttendingStates().add(this);
    }

    public void setExam(Exam exam) {
        this.exam = exam;
        if (exam != null)
            exam.getAttenderStates().add(this);
    }

    public void addAnswer(AttenderAnswer attenderAnswer) {
        this.answers.add(attenderAnswer);
        if (!this.equals(attenderAnswer.getAttenderState()))
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
