package kr.pullgo.pullgoserver.dto.mapper;

import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExamDtoMapper implements DtoMapper<Exam, ExamDto.Create, ExamDto.Result> {

    private final TeacherDtoMapper teacherDtoMapper;

    @Autowired
    public ExamDtoMapper(TeacherDtoMapper teacherDtoMapper) {
        this.teacherDtoMapper = teacherDtoMapper;
    }

    @Override
    public Exam asEntity(ExamDto.Create dto) {
        return Exam.builder()
            .name(dto.getName())
            .beginDateTime(dto.getBeginDateTime())
            .endDateTime(dto.getEndDateTime())
            .timeLimit(dto.getTimeLimit())
            .build();
    }

    @Override
    public ExamDto.Result asResultDto(Exam exam) {
        return ExamDto.Result.builder()
            .id(exam.getId())
            .classroomId(exam.getClassroom().getId())
            .creator(teacherDtoMapper.asResultDto(exam.getCreator()))
            .name(exam.getName())
            .beginDateTime(exam.getBeginDateTime())
            .endDateTime(exam.getEndDateTime())
            .timeLimit(exam.getTimeLimit())
            .passScore(exam.getPassScore())
            .cancelled(exam.isCancelled())
            .finished(exam.isFinished())
            .build();
    }

}
