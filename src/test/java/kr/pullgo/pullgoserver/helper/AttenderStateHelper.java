package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.ExamHelper.anExam;
import static kr.pullgo.pullgoserver.helper.StudentHelper.aStudent;

import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;

public class AttenderStateHelper {

    public static AttenderState anAttenderState() {
        AttenderState attenderState = AttenderState.builder()
            .examStartTime(LocalDateTime.now())
            .build();
        attenderState.setId(0L);
        attenderState.setAttender(aStudent());
        attenderState.setExam(anExam());
        return attenderState;
    }

    public static AttenderStateDto.Create anAttenderStateCreateDto() {
        return AttenderStateDto.Create.builder()
            .attenderId(0L)
            .examId(0L)
            .build();
    }

    public static AttenderStateDto.Update anAttenderStateUpdateDto() {
        return AttenderStateDto.Update.builder()
            .progress(AttendingProgress.COMPLETE)
            .score(85)
            .build();
    }

    public static AttenderStateDto.Result anAttenderStateResultDto() {
        return AttenderStateDto.Result.builder()
            .id(0L)
            .attenderId(0L)
            .examId(0L)
            .progress(AttendingProgress.COMPLETE)
            .score(85)
            .build();
    }

}
