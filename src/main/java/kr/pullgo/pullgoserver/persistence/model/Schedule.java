package kr.pullgo.pullgoserver.persistence.model;

import com.sun.istack.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
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
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ToString.Exclude
    @OneToOne(mappedBy = "schedule")
    private Lesson lesson;

    @NotNull
    private DayOfWeek dayOfWeek;

    @NotNull
    private LocalTime beginTime;

    @NotNull
    private LocalTime endTime;

    @Builder
    public Schedule(DayOfWeek dayOfWeek, LocalTime beginTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }
}
