package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroom;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacher;

import java.time.Duration;
import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.dto.ExamDto;
import kr.pullgo.pullgoserver.persistence.model.Exam;

public class ExamHelper {

    private static final String ARBITRARY_NAME = "기말시험";
    private static final LocalDateTime ARBITRARY_BEGIN_DATE_TIME =
        LocalDateTime.now().minusHours(1);
    private static final LocalDateTime ARBITRARY_END_DATE_TIME =
        LocalDateTime.now().plusHours(3);
    private static final Duration ARBITRARY_TIME_LIMIT = Duration.ofHours(1);

    public static Exam anExam() {
        Exam exam = Exam.builder()
            .name(ARBITRARY_NAME)
            .beginDateTime(ARBITRARY_BEGIN_DATE_TIME)
            .endDateTime(ARBITRARY_END_DATE_TIME)
            .timeLimit(ARBITRARY_TIME_LIMIT)
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
            .name(ARBITRARY_NAME)
            .beginDateTime(ARBITRARY_BEGIN_DATE_TIME)
            .endDateTime(ARBITRARY_END_DATE_TIME)
            .timeLimit(ARBITRARY_TIME_LIMIT)
            .build();
    }

    public static ExamDto.Update anExamUpdateDto() {
        return ExamDto.Update.builder()
            .name(ARBITRARY_NAME)
            .beginDateTime(ARBITRARY_BEGIN_DATE_TIME)
            .endDateTime(ARBITRARY_END_DATE_TIME)
            .timeLimit(ARBITRARY_TIME_LIMIT)
            .build();
    }

    public static ExamDto.Result anExamResultDto() {
        return ExamDto.Result.builder()
            .id(0L)
            .classroomId(0L)
            .creatorId(0L)
            .name(ARBITRARY_NAME)
            .beginDateTime(ARBITRARY_BEGIN_DATE_TIME)
            .endDateTime(ARBITRARY_END_DATE_TIME)
            .timeLimit(ARBITRARY_TIME_LIMIT)
            .cancelled(false)
            .finished(false)
            .build();
    }

}
