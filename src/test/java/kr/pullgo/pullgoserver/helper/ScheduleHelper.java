package kr.pullgo.pullgoserver.helper;

import java.time.LocalDate;
import java.time.LocalTime;
import kr.pullgo.pullgoserver.dto.ScheduleDto;
import kr.pullgo.pullgoserver.persistence.model.Schedule;

public class ScheduleHelper {

    public static Schedule aSchedule() {
        Schedule schedule = Schedule.builder()
            .date(LocalDate.of(2021, 2, 15))
            .beginTime(LocalTime.of(16, 0))
            .endTime(LocalTime.of(17, 0))
            .build();
        schedule.setId(0L);
        return schedule;
    }

    public static ScheduleDto.Create aScheduleCreateDto() {
        return ScheduleDto.Create.builder()
            .date(LocalDate.of(2021, 2, 22))
            .beginTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(13, 0))
            .build();
    }

    public static ScheduleDto.Update aScheduleUpdateDto() {
        return ScheduleDto.Update.builder()
            .date(LocalDate.of(2021, 2, 22))
            .beginTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(13, 0))
            .build();
    }

    public static ScheduleDto.Result aScheduleResultDto() {
        return ScheduleDto.Result.builder()
            .date(LocalDate.of(2021, 2, 22))
            .beginTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(13, 0))
            .build();
    }
}
