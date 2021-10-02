package kr.pullgo.pullgoserver.persistence.model;

import static javax.persistence.FetchType.LAZY;

import com.sun.istack.NotNull;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import kr.pullgo.pullgoserver.persistence.converter.AnswerConverter;
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
public class Question extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @NotNull
    @ManyToOne(fetch = LAZY)
    private Exam exam;

    @NotNull
    private String content;

    private String pictureUrl;

    @NotNull
    @Convert(converter = AnswerConverter.class)
    private Answer answer;

    @Builder
    public Question(String content, String pictureUrl,
        Answer answer) {
        this.content = content;
        this.pictureUrl = pictureUrl;
        this.answer = answer;
    }

    protected Question(Long id, Exam exam, String content, String pictureUrl,
        Answer answer) {
        this.id = id;
        setExam(exam);
        this.content = content;
        this.pictureUrl = pictureUrl;
        this.answer = answer;
    }

    public void setExam(Exam exam) {
        if (this.exam != exam) {
            if (this.exam != null)
                this.exam.removeQuestion(this);
            this.exam = exam;
            if (exam != null) {
                exam.addQuestion(this);
            }
        }
    }
}
