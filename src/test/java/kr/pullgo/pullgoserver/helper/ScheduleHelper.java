package kr.pullgo.pullgoserver.helper;

import java.time.LocalDate;
import java.time.LocalTime;
import kr.pullgo.pullgoserver.dto.ScheduleDto;
import kr.pullgo.pullgoserver.persistence.model.Schedule;

public class ScheduleHelper {

    private final static LocalDate ARBITRARY_DATE = LocalDate.of(2021, 3, 8);
    private final static LocalTime ARBITRARY_BEGIN_TIME = LocalTime.of(16, 0);
    private final static LocalTime ARBITRARY_END_TIME = LocalTime.of(17, 30);

    public static Schedule aSchedule() {
        Schedule schedule = Schedule.builder()
            .date(ARBITRARY_DATE)
            .beginTime(ARBITRARY_BEGIN_TIME)
            .endTime(ARBITRARY_END_TIME)
            .build();
        schedule.setId(0L);
        return schedule;
    }

    public static ScheduleDto.Create aScheduleCreateDto() {
        return ScheduleDto.Create.builder()
            .date(ARBITRARY_DATE)
            .beginTime(ARBITRARY_BEGIN_TIME)
            .endTime(ARBITRARY_END_TIME)
            .build();
    }

    public static ScheduleDto.Update aScheduleUpdateDto() {
        return ScheduleDto.Update.builder()
            .date(ARBITRARY_DATE)
            .beginTime(ARBITRARY_BEGIN_TIME)
            .endTime(ARBITRARY_END_TIME)
            .build();
    }

    public static ScheduleDto.Result aScheduleResultDto() {
        return ScheduleDto.Result.builder()
            .date(ARBITRARY_DATE)
            .beginTime(ARBITRARY_BEGIN_TIME)
            .endTime(ARBITRARY_END_TIME)
            .build();
    }
}
