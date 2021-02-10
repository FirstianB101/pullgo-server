package kr.pullgo.pullgoserver.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.dto.AcademyDto.KickStudent;
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

    @MockBean
    private AcademyService academyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getAcademy() throws Exception {
        // Given
        AcademyDto.Result result = AcademyDto.Result.builder()
            .id(0L)
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();

        given(academyService.getAcademy(anyLong()))
            .willReturn(result);

        // When
        ResultActions actions = mockMvc.perform(get("/academies/0"))
            .andDo(print());

        // Then
        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(0L))
            .andExpect(jsonPath("$.name").value("Test academy"))
            .andExpect(jsonPath("$.phone").value("010-1234-5678"))
            .andExpect(jsonPath("$.address").value("Seoul"));
    }

    @Test
    void postAcademy() throws Exception {
        // Given
        given(academyService.createAcademy(any()))
            .willReturn(AcademyDto.Result.builder()
                .id(0L)
                .name("Test academy")
                .phone("010-1234-5678")
                .address("Seoul")
                .build());

        // When
        AcademyDto.Create body = AcademyDto.Create.builder()
            .name("Test academy")
            .phone("010-1234-5678")
            .address("Seoul")
            .build();

        ResultActions actions = mockMvc.perform(post("/academies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
            .andDo(print());

        // Then
        actions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(0L));
    }

    @Test
    void patchAcademy() throws Exception {
        // Given
        given(academyService.updateAcademy(anyLong(), any()))
            .willReturn(AcademyDto.Result.builder()
                .id(0L)
                .name("Test academy")
                .phone("010-1234-5678")
                .address("Seoul")
                .build());

        // When
        AcademyDto.Update body = AcademyDto.Update.builder()
            .name("Test academy")
            .build();

        ResultActions actions = mockMvc.perform(patch("/academies/0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
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
        verify(academyService).deleteAcademy(0L);

        actions
            .andExpect(status().isNoContent());
    }

    @Test
    void acceptTeacher() throws Exception {
        // When
        ResultActions actions = mockMvc.perform(post("/academies/0/accept-teacher")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"teacherId\":0}"))
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
            .content("{\"teacherId\":0}"))
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
            .content("{\"studentId\":0}"))
            .andDo(print());

        // Then
        verify(academyService).acceptStudent(eq(0L), any());

        actions
            .andExpect(status().isNoContent());
    }

    @Test
    void kickStudent() throws Exception {
        // When
        AcademyDto.KickStudent body = KickStudent.builder()
            .studentId(0L)
            .build();

        ResultActions actions = mockMvc.perform(post("/academies/0/kick-student")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
            .andDo(print());

        // Then
        verify(academyService).kickStudent(eq(0L), any(AcademyDto.KickStudent.class));

        actions
            .andExpect(status().isNoContent());
    }
}