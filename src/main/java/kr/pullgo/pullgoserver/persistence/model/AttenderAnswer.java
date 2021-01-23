package kr.pullgo.pullgoserver.persistence.model;

import com.sun.istack.NotNull;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import kr.pullgo.pullgoserver.persistence.converter.AnswerConverter;
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
public class AttenderAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @NotNull
    @ManyToOne
    private AttenderState State;

    @ToString.Exclude
    @NotNull
    @ManyToOne
    private Question question;

    @NotNull
    @Convert(converter = AnswerConverter.class)
    private Answer answer;

    @Builder
    public AttenderAnswer(Answer answer) {
        this.answer = answer;
    }
}
