package kr.pullgo.pullgoserver.presentation.controller;

import static kr.pullgo.pullgoserver.helper.AcademyHelper.academyCreateDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.academyResultDtoWithId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.academyUpdateDto;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptStudentDtoWithStudentId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.acceptTeacherDtoWithTeacherId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickStudentDtoWithStudentId;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.kickTeacherDtoWithTeacherId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.service.AcademyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(value = AcademyController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class})
class AcademyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AcademyService academyService;

    @Test
    void getAcademy() throws Exception {
        // Given
        AcademyDto.Result result = AcademyDto.Result.builder()
            .id(0L)
            .name("Test academy")
            .phone("01012345678")
            .address("Seoul")
            .ownerId(1L)
            .build();

        given(academyService.read(eq(0L)))
            .willReturn(result);

        // When
        ResultActions actions = mockMvc.perform(get("/academies/0"))
            .andDo(print());

        // Then
        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(0L))
            .andExpect(jsonPath("$.name").value("Test academy"))
            .andExpect(jsonPath("$.phone").value("01012345678"))
            .andExpect(jsonPath("$.address").value("Seoul"))
            .andExpect(jsonPath("$.ownerId").value(1L));
    }

    @Test
    void postAcademy() throws Exception {
        // Given
        given(academyService.create(any()))
            .willReturn(academyResultDtoWithId(0L));

        // When
        ResultActions actions = mockMvc.perform(post("/academies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(academyCreateDto())))
            .andDo(print());

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(0L));
    }

    @Test
    void patchAcademy() throws Exception {
        // Given
        given(academyService.update(eq(0L), any()))
            .willReturn(academyResultDtoWithId(0L));

        // When
        ResultActions actions = mockMvc.perform(patch("/academies/0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(academyUpdateDto())))
            .andDo(print());

        // Then
        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(0L));
    }

    @Test
    void deleteAcademy() throws Exception {
        // When
        ResultActions actions = mockMvc.perform(delete("/academies/0"))
            .andDo(print());

        // Then
        verify(academyService).delete(0L);

        actions
            .andExpect(status().isNoContent());
    }

    @Test
    void acceptTeacher() throws Exception {
        // When
        ResultActions actions = mockMvc.perform(post("/academies/0/accept-teacher")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(acceptTeacherDtoWithTeacherId(0L))))
            .andDo(print());

        // Then
        verify(academyService).acceptTeacher(eq(0L), any());

        actions
            .andExpect(status().isNoContent());
    }

    @Test
    void kickTeacher() throws Exception {
        // When
        ResultActions actions = mockMvc.perform(post("/academies/0/kick-teacher")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(kickTeacherDtoWithTeacherId(0L))))
            .andDo(print());

        // Then
        verify(academyService).kickTeacher(eq(0L), any());

        actions
            .andExpect(status().isNoContent());
    }

    @Test
    void acceptStudent() throws Exception {
        // When
        ResultActions actions = mockMvc.perform(post("/academies/0/accept-student")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(acceptStudentDtoWithStudentId(0L))))
            .andDo(print());

        // Then
        verify(academyService).acceptStudent(eq(0L), any());

        actions
            .andExpect(status().isNoContent());
    }

    @Test
    void kickStudent() throws Exception {
        // When
        ResultActions actions = mockMvc.perform(post("/academies/0/kick-student")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(kickStudentDtoWithStudentId(0L))))
            .andDo(print());

        // Then
        verify(academyService).kickStudent(eq(0L), any(AcademyDto.KickStudent.class));

        actions
            .andExpect(status().isNoContent());
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}