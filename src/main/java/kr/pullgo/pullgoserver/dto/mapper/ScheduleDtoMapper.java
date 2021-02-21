package kr.pullgo.pullgoserver.dto.mapper;

import kr.pullgo.pullgoserver.dto.ScheduleDto;
import kr.pullgo.pullgoserver.persistence.model.Schedule;
import org.springframework.stereotype.Component;

@Component
public class ScheduleDtoMapper implements
    DtoMapper<Schedule, ScheduleDto.Create, ScheduleDto.Result> {

    @Override
    public Schedule asEntity(ScheduleDto.Create dto) {
        return Schedule.builder()
            .date(dto.getDate())
            .beginTime(dto.getBeginTime())
            .endTime(dto.getEndTime())
            .build();
    }

    @Override
    public ScheduleDto.Result asResultDto(Schedule schedule) {
        return ScheduleDto.Result.builder()
            .id(schedule.getId())
            .date(schedule.getDate())
            .beginTime(schedule.getBeginTime())
            .endTime(schedule.getEndTime())
            .build();
    }

}
