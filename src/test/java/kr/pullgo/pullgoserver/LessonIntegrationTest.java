package kr.pullgo.pullgoserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.dto.LessonDto;
import kr.pullgo.pullgoserver.dto.LessonDto.Update;
import kr.pullgo.pullgoserver.dto.ScheduleDto;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import kr.pullgo.pullgoserver.persistence.model.Schedule;
import kr.pullgo.pullgoserver.persistence.repository.ClassroomRepository;
import kr.pullgo.pullgoserver.persistence.repository.LessonRepository;
import kr.pullgo.pullgoserver.util.H2DbCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class LessonIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        H2DbCleaner.clean(dataSource);
    }

    @Nested
    class GetLesson {

        @Test
        void getLesson() throws Exception {
            // Given
            Lesson lesson = Lesson.builder()
                .name("test name")
                .build();
            Schedule schedule = Schedule.builder()
                .date(stringToLocalDate("1111-11-11"))
                .beginTime(stringToLocalTime("22:22:22"))
                .endTime(stringToLocalTime("00:00:00"))
                .build();
            Classroom classroom = createClassroom();

            lesson.setSchedule(schedule);
            classroom.addLesson(lesson);

            classroomRepository.save(classroom);

            // When
            ResultActions actions = mockMvc
                .perform(get("/academy/classroom/lessons/{id}", lesson.getId()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lesson.getId()))
                .andExpect(jsonPath("$.name").value("test name"))
                .andExpect(jsonPath("$.classroomId").value(classroom.getId()))
                .andExpect(jsonPath("$.schedule.date").value("1111-11-11"))
                .andExpect(jsonPath("$.schedule.beginTime").value("22:22:22"))
                .andExpect(jsonPath("$.schedule.endTime").value("00:00:00"));
        }

        @Test
        void getLesson_LessonNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/academy/classroom/lessons/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Test
    void postLesson() throws Exception {
        // Given
        Classroom classroom = createAndSaveClassroom();

        // When
        LessonDto.Create dto = LessonDto.Create.builder()
            .name("test name")
            .classroomId(classroom.getId())
            .schedule(ScheduleDto.Create.builder()
                .date(stringToLocalDate("1111-11-11"))
                .beginTime(stringToLocalTime("22:22:22"))
                .endTime(stringToLocalTime("00:00:00"))
                .build())
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/academy/classroom/lessons")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.classroomId").value(classroom.getId()))
            .andExpect(jsonPath("$.name").value("test name"))
            .andExpect(jsonPath("$.schedule.date").value("1111-11-11"))
            .andExpect(jsonPath("$.schedule.beginTime").value("22:22:22"))
            .andExpect(jsonPath("$.schedule.endTime").value("00:00:00"));
    }

    @Nested
    class PatchLesson {

        @Test
        void patchLesson() throws Exception {
            // Given
            Lesson lesson = Lesson.builder()
                .name("before name")
                .build();

            Schedule schedule = Schedule.builder()
                .date(stringToLocalDate("1111-11-11"))
                .beginTime(stringToLocalTime("22:22:22"))
                .endTime(stringToLocalTime("00:00:00"))
                .build();
            Classroom classroom = createClassroom();

            lesson.setSchedule(schedule);
            classroom.addLesson(lesson);

            classroomRepository.save(classroom);

            // When
            LessonDto.Update dto = Update.builder()
                .name("test name")
                .schedule(ScheduleDto.Update.builder()
                    .date(stringToLocalDate("2021-03-02"))
                    .beginTime(stringToLocalTime("08:00:00"))
                    .endTime(stringToLocalTime("21:59:59"))
                    .build())
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc
                .perform(patch("/academy/classroom/lessons/{id}", lesson.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lesson.getId()))
                .andExpect(jsonPath("$.name").value("test name"))
                .andExpect(jsonPath("$.classroomId").value(classroom.getId()))
                .andExpect(jsonPath("$.schedule.date").value("2021-03-02"))
                .andExpect(jsonPath("$.schedule.beginTime").value("08:00:00"))
                .andExpect(jsonPath("$.schedule.endTime").value("21:59:59"));

        }

        @Test
        void patchLesson_LessonNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(lessonUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/academy/classroom/lessons/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteLesson {

        @Test
        void deleteLesson() throws Exception {
            // Given
            Lesson lesson = createAndSaveLesson();

            // When
            ResultActions actions = mockMvc
                .perform(delete("/academy/classroom/lessons/{id}", lesson.getId()));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(lessonRepository.findById(lesson.getId())).isEmpty();
        }

        @Test
        void deleteLesson_LessonNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/academy/classroom/lessons/{id}", 0));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private LocalDate stringToLocalDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }

    private LocalTime stringToLocalTime(String date) {
        return LocalTime.parse(date);
    }

    private Classroom createClassroom() {
        return Classroom.builder()
            .name("test classroom")
            .build();
    }

    private Classroom createAndSaveClassroom() {
        return classroomRepository.save(Classroom.builder()
            .name("test classroom")
            .build());
    }

    private Schedule createSchedule() {
        return Schedule.builder()
            .date(stringToLocalDate("2021-03-02"))
            .beginTime(stringToLocalTime("08:00:00"))
            .endTime(stringToLocalTime("21:59:59"))
            .build();
    }

    private Lesson createAndSaveLesson() {
        Lesson lesson = Lesson.builder()
            .name("test lesson")
            .build();

        Schedule schedule = createSchedule();
        Classroom classroom = createClassroom();
        lesson.setSchedule(schedule);
        classroom.addLesson(lesson);

        classroomRepository.save(classroom);
        return lesson;
    }

    private LessonDto.Update lessonUpdateDto() {
        return LessonDto.Update.builder()
            .name("test name")
            .build();
    }
}
