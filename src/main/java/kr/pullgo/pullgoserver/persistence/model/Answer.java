package kr.pullgo.pullgoserver.persistence.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Data
public class Answer extends TimeEntity {

    private Set<Integer> objectiveNumbers = new HashSet<>();

    public Answer(Set<Integer> objectiveNumbers) {
        this.objectiveNumbers = objectiveNumbers;
    }

    public Answer(int... numbers) {
        for (int number : numbers) {
            objectiveNumbers.add(number);
        }
    }
}
