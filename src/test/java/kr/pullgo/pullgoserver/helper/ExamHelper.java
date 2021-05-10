package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroom;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacher;

import java.time.Duration;
import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.persistence.model.Exam;

public class ExamHelper {

    public static Exam anExam() {
        Exam exam = Exam.builder()
            .name("Test")
            .beginDateTime(LocalDateTime.of(2021, 1, 28, 0, 0))
            .endDateTime(LocalDateTime.of(2021, 1, 29, 0, 0))
            .timeLimit(Duration.ZERO)
            .build();
        exam.setId(0L);
        exam.setClassroom(aClassroom());
        exam.setCreator(aTeacher());
        return exam;
    }

    public static ExamDto.Create anExamCreateDto() {
        return ExamDto.Create.builder()
            .classroomId(0L)
            .creatorId(0L)
            .name("test name")
            .beginDateTime(LocalDateTime.of(2021, 1, 28, 0, 0))
            .endDateTime(LocalDateTime.of(2021, 1, 29, 0, 0))
            .timeLimit(Duration.ZERO)
            .build();
    }

    public static ExamDto.Update anExamUpdateDto() {
        return ExamDto.Update.builder()
            .name("test name")
            .beginDateTime(LocalDateTime.of(2021, 1, 28, 0, 0))
            .endDateTime(LocalDateTime.of(2021, 1, 29, 0, 0))
            .timeLimit(Duration.ZERO)
            .build();
    }

    public static ExamDto.Result anExamResultDto() {
        return ExamDto.Result.builder()
            .id(0L)
            .classroomId(0L)
            .creatorId(0L)
            .name("test name")
            .beginDateTime(LocalDateTime.of(2021, 1, 28, 0, 0))
            .endDateTime(LocalDateTime.of(2021, 1, 29, 0, 0))
            .timeLimit(Duration.ZERO)
            .cancelled(false)
            .finished(false)
            .build();
    }

}
