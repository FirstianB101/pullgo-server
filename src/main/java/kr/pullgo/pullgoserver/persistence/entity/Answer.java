package kr.pullgo.pullgoserver.persistence.entity;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Data
public class Answer {

    private Set<Integer> objectiveNumbers = new HashSet<>();
}
