package kr.pullgo.pullgoserver.service.exam;

import java.util.function.Consumer;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.service.cron.CronJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamCronJobService {

    private final String CRON_NAME = "exam";
    private final CronJob cronJob;

    public void registerExamCronJob(Exam exam, Consumer<Exam> job) {
        cronJob.register(exam.getId(), () -> job.accept(exam), exam.getExamEndTime(), CRON_NAME);
    }

    @Transactional
    public void removeExamCronJob(Exam exam) {
        cronJob.remove(exam.getId(), CRON_NAME);
    }

}
