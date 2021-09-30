package kr.pullgo.pullgoserver.persistence.model;

import static javax.persistence.FetchType.LAZY;

import com.sun.istack.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
public class Exam extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    private Classroom classroom;

    @ToString.Exclude
    @NotNull
    @ManyToOne(fetch = LAZY)
    private Teacher creator;

    @NotNull
    private String name;

    @NotNull
    private LocalDateTime beginDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    @NotNull
    private Duration timeLimit;

    private Integer passScore;

    private boolean cancelled = false;

    private boolean finished = false;

    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> questions = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "exam", cascade = CascadeType.REMOVE)
    private Set<AttenderState> attenderStates = new HashSet<>();

    @Builder
    public Exam(String name, LocalDateTime beginDateTime, LocalDateTime endDateTime,
        Duration timeLimit, Integer passScore) {
        this.name = name;
        this.beginDateTime = beginDateTime;
        this.endDateTime = endDateTime;
        this.timeLimit = timeLimit;
        this.passScore = passScore;
    }

    protected Exam(Long id, Classroom classroom, Teacher creator, String name,
        LocalDateTime beginDateTime, LocalDateTime endDateTime, Duration timeLimit,
        Integer passScore, boolean cancelled, boolean finished,
        Set<Question> questions,
        Set<AttenderState> attenderStates) {
        this.id = id;
        setClassroom(classroom);
        this.creator = creator;
        this.name = name;
        this.beginDateTime = beginDateTime;
        this.endDateTime = endDateTime;
        this.timeLimit = timeLimit;
        this.passScore = passScore;
        this.cancelled = cancelled;
        this.finished = finished;
        this.questions = questions;
        this.attenderStates = attenderStates;
    }

    public void addQuestion(Question question) {
        questions.add(question);
        if (!this.equals(question.getExam()))
            question.setExam(this);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        if (classroom != null)
            classroom.addExam(this);
    }
}
