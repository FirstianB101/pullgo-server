package kr.pullgo.pullgoserver.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.dto.ScheduleDto;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import kr.pullgo.pullgoserver.persistence.model.Schedule;
import kr.pullgo.pullgoserver.persistence.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;

    @Autowired
    public LessonService(
        LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    @Transactional
    public LessonDto.Result createLesson(LessonDto.Create dto) {
        Lesson lesson = lessonRepository.save(LessonDto.mapToEntity(dto));
        return LessonDto.mapFromEntity(lesson);
    }

    @Transactional
    public LessonDto.Result updateLesson(Long id, LessonDto.Update dto) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson id was not found"));

        ScheduleDto.Update dtoSchedule = dto.getSchedule();
        Schedule entitySchedule = lesson.getSchedule();
        if (dto.getName() != null) { lesson.setName(dto.getName()); }
        if (dtoSchedule != null) {
            if (dtoSchedule.getDate() != null) { entitySchedule.setDate(dtoSchedule.getDate()); }
            if (dtoSchedule.getBeginTime() != null) {
                entitySchedule.setBeginTime(dtoSchedule.getBeginTime());
            }
            if (dtoSchedule.getEndTime() != null) {
                entitySchedule.setEndTime(dtoSchedule.getEndTime());
            }
        }
        lesson = lessonRepository.save(lesson);
        return LessonDto.mapFromEntity(lesson);
    }

    @Transactional
    public void deleteLesson(Long id) {
        int deleteResult = lessonRepository.removeById(id);
        if (deleteResult == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson id was not found");
        }
    }

    @Transactional
    public LessonDto.Result getLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson id was not found"));
        return LessonDto.mapFromEntity(lesson);
    }

    @Transactional
    public List<LessonDto.Result> getLessons() {
        List<Lesson> lessons = lessonRepository.findAll();
        return lessons.stream()
            .map(LessonDto::mapFromEntity)
            .collect(Collectors.toList());
    }
}
