package kr.pullgo.pullgoserver.persistence.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

import com.sun.istack.NotNull;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
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
public class Lesson extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @NotNull
    @ManyToOne(fetch = LAZY)
    private Classroom classroom;

    @NotEmpty
    private String name;

    @NotNull
    @ToString.Exclude
    @OneToOne(cascade = ALL, fetch = LAZY)
    private Schedule schedule;

    @Builder
    public Lesson(String name) {
        this.name = name;
    }

    protected Lesson(Long id, Classroom classroom, String name,
        Schedule schedule) {
        this.id = id;
        this.name = name;
        setClassroom(classroom);
        setSchedule(schedule);
    }

    public void setSchedule(Schedule schedule) {
        if (this.schedule != schedule) {
            if (this.schedule != null)
                this.schedule.setLesson(null);
            this.schedule = schedule;
            if (schedule != null)
                schedule.setLesson(this);
        }
    }

    public void setClassroom(Classroom classroom) {
        if (this.classroom != classroom) {
            if (this.classroom != null)
                this.classroom.removeLesson(this);
            this.classroom = classroom;
            if (classroom != null) {
                classroom.addLesson(this);
            }
        }
    }
}
