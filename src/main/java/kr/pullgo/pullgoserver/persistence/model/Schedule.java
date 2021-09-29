package kr.pullgo.pullgoserver.persistence.model;

import static javax.persistence.FetchType.*;

import com.sun.istack.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
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
public class Schedule extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ToString.Exclude
    @OneToOne(mappedBy = "schedule", fetch = LAZY)
    private Lesson lesson;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime beginTime;

    @NotNull
    private LocalTime endTime;

    @Builder
    public Schedule(LocalDate date, LocalTime beginTime, LocalTime endTime) {
        this.date = date;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }
}
