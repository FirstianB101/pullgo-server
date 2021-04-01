package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.dto.ScheduleDto;
import kr.pullgo.pullgoserver.dto.mapper.LessonDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import kr.pullgo.pullgoserver.persistence.model.Schedule;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.LessonRepository;
import kr.pullgo.pullgoserver.util.ResponseStatusExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LessonService extends
    BaseCrudService<Lesson, Long, LessonDto.Create, LessonDto.Update, LessonDto.Result> {

    private final LessonDtoMapper dtoMapper;
    private final LessonRepository lessonRepository;
    private final ClassroomRepository classroomRepository;

    @Autowired
    public LessonService(LessonDtoMapper dtoMapper,
        LessonRepository lessonRepository,
        ClassroomRepository classroomRepository) {
        super(Lesson.class, dtoMapper, lessonRepository);
        this.dtoMapper = dtoMapper;
        this.lessonRepository = lessonRepository;
        this.classroomRepository = classroomRepository;
    }

    @Override
    Lesson createOnDB(LessonDto.Create dto) {
        Lesson lesson = dtoMapper.asEntity(dto);
        Classroom classroom = classroomRepository.findById(dto.getClassroomId())
            .orElseThrow(ResponseStatusExceptions::classroomNotFound);
        classroom.addLesson(lesson);
        return lessonRepository.save(lesson);
    }

    @Override
    Lesson updateOnDB(Lesson entity, LessonDto.Update dto) {
        ScheduleDto.Update dtoSchedule = dto.getSchedule();
        Schedule entitySchedule = entity.getSchedule();
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dtoSchedule != null) {
            if (dtoSchedule.getDate() != null) {
                entitySchedule.setDate(dtoSchedule.getDate());
            }
            if (dtoSchedule.getBeginTime() != null) {
                entitySchedule.setBeginTime(dtoSchedule.getBeginTime());
            }
            if (dtoSchedule.getEndTime() != null) {
                entitySchedule.setEndTime(dtoSchedule.getEndTime());
            }
        }
        return lessonRepository.save(entity);
    }

    @Override
    int removeOnDB(Long id) {
        return lessonRepository.removeById(id);
    }
}
