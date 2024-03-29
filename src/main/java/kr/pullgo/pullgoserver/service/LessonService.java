package kr.pullgo.pullgoserver.service;

import java.util.List;
import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.dto.ScheduleDto;
import kr.pullgo.pullgoserver.dto.mapper.LessonDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import kr.pullgo.pullgoserver.persistence.model.Schedule;
import kr.pullgo.pullgoserver.persistence.repository.LessonRepository;
import kr.pullgo.pullgoserver.service.authorizer.LessonAuthorizer;
import kr.pullgo.pullgoserver.service.helper.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LessonService {

    private final LessonDtoMapper dtoMapper;
    private final LessonRepository lessonRepository;
    private final RepositoryHelper repoHelper;
    private final LessonAuthorizer lessonAuthorizer;

    @Autowired
    public LessonService(LessonDtoMapper dtoMapper,
        LessonRepository lessonRepository,
        RepositoryHelper repoHelper,
        LessonAuthorizer lessonAuthorizer) {
        this.dtoMapper = dtoMapper;
        this.lessonRepository = lessonRepository;
        this.repoHelper = repoHelper;
        this.lessonAuthorizer = lessonAuthorizer;
    }

    @Transactional
    public LessonDto.Result create(LessonDto.Create dto, Authentication authentication) {
        Lesson lesson = dtoMapper.asEntity(dto);

        Classroom classroom = repoHelper.findClassroomOrThrow(dto.getClassroomId());
        classroom.addLesson(lesson);

        lessonAuthorizer.requireClassroomTeacher(authentication, lesson);

        return dtoMapper.asResultDto(lessonRepository.save(lesson));
    }

    @Transactional(readOnly = true)
    public LessonDto.Result read(Long id) {
        Lesson entity = repoHelper.findLessonOrThrow(id);
        return dtoMapper.asResultDto(entity);
    }

    @Transactional(readOnly = true)
    public List<LessonDto.Result> search(Specification<Lesson> spec, Pageable pageable) {
        Page<Lesson> entities = lessonRepository.findAll(spec, pageable);
        return dtoMapper.asResultDto(entities);
    }

    @Transactional
    public LessonDto.Result update(Long id, LessonDto.Update dto, Authentication authentication) {
        Lesson entity = repoHelper.findLessonOrThrow(id);
        lessonAuthorizer.requireClassroomTeacher(authentication, entity);

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
        return dtoMapper.asResultDto(lessonRepository.save(entity));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Lesson entity = repoHelper.findLessonOrThrow(id);
        lessonAuthorizer.requireClassroomTeacher(authentication, entity);

        lessonRepository.delete(entity);
    }
}
