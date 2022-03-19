package kr.pullgo.pullgoserver.service.exam;

import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.service.authorizer.ExamAuthorizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamFinishService {

    private final ExamAuthorizer examAuthorizer;
    private final ExamCronJobService examCronJobService;
    private final OnGoingExamFindService onGoingExamFindService;

    @Autowired
    public ExamFinishService(
        ExamAuthorizer examAuthorizer,
        ExamCronJobService examCronJobService,
        OnGoingExamFindService onGoingExamFindService) {
        this.examAuthorizer = examAuthorizer;
        this.examCronJobService = examCronJobService;
        this.onGoingExamFindService = onGoingExamFindService;
    }

    @PostConstruct
    public void init() {
        this.registerCronJobsByOnGoingStatusExams();
    }

    public void registerCronJobsByOnGoingStatusExams() {
        onGoingExamFindService.getOnGoingExams().forEach(
            exam -> examCronJobService.registerExamCronJob(exam, this::finishExam));
    }

    @Transactional
    public void finishExam(Long id, Authentication authentication) {
        Exam exam = onGoingExamFindService.getOnGoingExam(id);
        examAuthorizer.requireCreator(authentication, exam);

        finishExam(exam);
        examCronJobService.removeExamCronJob(exam);
    }

    @Transactional
    public void finishExam(Exam exam) {
        exam.getAttenderStates().stream().filter(attenderState ->
                attenderState.getProgress() == AttendingProgress.ONGOING)
            .forEach(AttenderState::mark);
        exam.setFinished(true);
    }

    @Transactional
    public void finishAllExam() {
        onGoingExamFindService.getOnGoingExams().filter(exam ->
            exam.getExamEndTime().isBefore(LocalDateTime.now())).forEach(
            ExamFinishService.this::finishExam
        );
    }
}
