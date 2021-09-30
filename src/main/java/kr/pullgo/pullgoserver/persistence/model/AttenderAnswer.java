package kr.pullgo.pullgoserver.persistence.model;

import static javax.persistence.FetchType.*;

import com.sun.istack.NotNull;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@With
@ToString
@Entity
public class AttenderAnswer extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @NotNull
    @ManyToOne(fetch = LAZY)
    private AttenderState attenderState;

    @ToString.Exclude
    @NotNull
    @ManyToOne(fetch = LAZY)
    private Question question;

    @NotNull
    @Convert(converter = AnswerConverter.class)
    private Answer answer;

    @Builder
    public AttenderAnswer(Answer answer) {
        this.answer = answer;
    }

    public void setAttenderState(AttenderState attenderState) {
        this.attenderState = attenderState;
        attenderState.addAnswer(this);
    }
}
