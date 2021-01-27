package kr.pullgo.pullgoserver.persistence.model;

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
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Entity
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne
    private Classroom classroom;

    @ToString.Exclude
    @NotNull
    @ManyToOne
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

    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> questions = new HashSet<>();

    @ToString.Exclude
    @NotNull
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
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
}
