package kr.pullgo.pullgoserver.service.exam;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.AttendingProgress;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.service.authorizer.ExamAuthorizer;
import kr.pullgo.pullgoserver.service.cron.CronJob;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamManagement {

    private final String CRON_NAME = "exam";
    private final CronJob cronJob;
    private final ExamAuthorizer examAuthorizer;
    private final ServiceErrorHelper errorHelper;
    private final RepositoryHelper repoHelper;
    private final ExamRepository examRepository;

    @Autowired
    public ExamManagement(CronJob cronJob,
        ExamAuthorizer examAuthorizer,
        ServiceErrorHelper errorHelper,
        RepositoryHelper repoHelper,
        ExamRepository examRepository) {
        this.cronJob = cronJob;
        this.examAuthorizer = examAuthorizer;
        this.errorHelper = errorHelper;
        this.repoHelper = repoHelper;
        this.examRepository = examRepository;
    }

    @PostConstruct
    public void init() {
        registerCronJobsByOnGoingStatusExams();
    }

    public void registerCronJob(Exam exam) {
        cronJob.register(exam.getId(), () -> finishExam(exam), exam.getExamEndTime(), CRON_NAME);
    }

    @Transactional
    public void removeCronJob(Exam exam) {
        cronJob.remove(exam.getId(), CRON_NAME);
    }

    public void registerCronJobsByOnGoingStatusExams() {
        getOnGoingExams().forEach(this::registerCronJob);
    }

    @Transactional
    public void cancelExam(Long id, Authentication authentication) {
        Exam exam = getOnGoingExam(id);
        examAuthorizer.requireCreator(authentication, exam);

        removeCronJob(exam);
        exam.setCancelled(true);
    }

    @Transactional
    public void finishExam(Long id, Authentication authentication) {
        Exam exam = getOnGoingExam(id);
        examAuthorizer.requireCreator(authentication, exam);

        finishExam(exam);
        removeCronJob(exam);
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
            ExamManagement.this::finishExam
        );
    }
}
