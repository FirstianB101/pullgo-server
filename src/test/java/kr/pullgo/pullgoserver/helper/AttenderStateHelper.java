package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.ExamHelper.anExam;
import static kr.pullgo.pullgoserver.helper.StudentHelper.aStudent;

import java.time.LocalDateTime;
import kr.pullgo.pullgoserver.dto.AttenderStateDto;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;

public class AttenderStateHelper {

    private static final AttendingProgress ARBITRARY_PROGRESS = AttendingProgress.COMPLETE;
    private static final int ARBITRARY_SCORE = 85;

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
            .progress(ARBITRARY_PROGRESS)
            .score(ARBITRARY_SCORE)
            .build();
    }

    public static AttenderStateDto.Result anAttenderStateResultDto() {
        return AttenderStateDto.Result.builder()
            .id(0L)
            .attenderId(0L)
            .examId(0L)
            .progress(ARBITRARY_PROGRESS)
            .score(ARBITRARY_SCORE)
            .build();
    }

}
