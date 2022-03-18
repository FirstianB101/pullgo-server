package kr.pullgo.pullgoserver.service.exam;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.service.authorizer.ExamAuthorizer;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamLifeCycleService {


    private final ExamAuthorizer examAuthorizer;
    private final ServiceErrorHelper errorHelper;
    private final RepositoryHelper repoHelper;
    private final ExamRepository examRepository;
    private final ExamCronJobService examCronJobService;

    @Autowired
    public ExamLifeCycleService(
        ExamAuthorizer examAuthorizer,
        ServiceErrorHelper errorHelper,
        RepositoryHelper repoHelper,
        ExamRepository examRepository,
        ExamCronJobService examCronJobService) {
        this.examAuthorizer = examAuthorizer;
        this.errorHelper = errorHelper;
        this.repoHelper = repoHelper;
        this.examRepository = examRepository;
        this.examCronJobService = examCronJobService;
    }

    @PostConstruct
    public void init() {
        this.registerCronJobsByOnGoingStatusExams();
    }

    public void registerCronJobsByOnGoingStatusExams() {
        getOnGoingExams().forEach(
            exam -> examCronJobService.registerExamCronJob(exam, this::finishExam));
    }

    @Transactional
    public void cancelExam(Long id, Authentication authentication) {
        Exam exam = getOnGoingExam(id);
        examAuthorizer.requireCreator(authentication, exam);

        examCronJobService.removeExamCronJob(exam);
        exam.setCancelled(true);
    }

    @Transactional
    public void finishExam(Long id, Authentication authentication) {
        Exam exam = getOnGoingExam(id);
        examAuthorizer.requireCreator(authentication, exam);

        finishExam(exam);
        examCronJobService.removeExamCronJob(exam);
    }

    @Transactional
    public Stream<Exam> getOnGoingExams() {
        return examRepository.findAll().stream().filter(Exam::isOnGoing);
    }

    @Transactional
    public Exam getOnGoingExam(Long id) {
        Exam exam = repoHelper.findExamOrThrow(id);
        if (exam.isFinished()) {
            throw errorHelper.badRequest("Exam already finished");
        }
        if (exam.isCancelled()) {
            throw errorHelper.badRequest("Exam already cancelled");
        }
        return exam;
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
        getOnGoingExams().filter(exam ->
            exam.getExamEndTime().isBefore(LocalDateTime.now())).forEach(
            ExamLifeCycleService.this::finishExam
        );
    }
}
