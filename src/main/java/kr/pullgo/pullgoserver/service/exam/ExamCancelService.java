package kr.pullgo.pullgoserver.service.exam;

import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.service.authorizer.ExamAuthorizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamCancelService {

    private final ExamAuthorizer examAuthorizer;
    private final ExamCronJobService examCronJobService;
    private final OnGoingExamFindService onGoingExamFindService;


    @Transactional
    public void cancelExam(Long id, Authentication authentication) {
        Exam exam = onGoingExamFindService.getOnGoingExam(id);
        examAuthorizer.requireCreator(authentication, exam);

        examCronJobService.removeExamCronJob(exam);
        exam.setCancelled(true);
    }
}
