package kr.pullgo.pullgoserver.persistence.model;

import com.sun.istack.NotNull;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@Entity
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @NotNull
    @ManyToOne
    private Classroom classroom;

    @NotNull
    private String name;

    @NotNull
    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    private Schedule schedule;

    @Builder
    public Lesson(String name) {
        this.name = name;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
        schedule.setLesson(this);
    }
}
