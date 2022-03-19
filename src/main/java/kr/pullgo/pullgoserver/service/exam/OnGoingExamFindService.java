package kr.pullgo.pullgoserver.service.exam;

import java.util.stream.Stream;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.repository.ExamRepository;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnGoingExamFindService {

    private final ServiceErrorHelper errorHelper;
    private final RepositoryHelper repoHelper;
    private final ExamRepository examRepository;

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

}
