package kr.pullgo.pullgoserver.persistence.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MultipleChoice {

    private Map<String , String> choices = new HashMap<>();

    public MultipleChoice(String... choices) {
        for (int i = 0; i < choices.length; i++) {
            this.choices.put(String.valueOf(i+1), choices[i]);
        }
    }

    public MultipleChoice(Map<String, String> choices) {
        this.choices=choices;
    }
}
