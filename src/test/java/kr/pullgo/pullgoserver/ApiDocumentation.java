package kr.pullgo.pullgoserver;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.RequestDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ApiDocumentation {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(documentationConfiguration(restDocumentation))
            .build();
    }

    @Test
    void errorExample() throws Exception {
        // When
        ResultActions actions = mockMvc.perform(get("/error")
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
            .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/students")
            .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Student id was not found"));

        // Then
        actions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("error").value("Not Found"))
            .andExpect(jsonPath("timestamp").value(notNullValue()))
            .andExpect(jsonPath("status").value(404))
            .andExpect(jsonPath("path").value(notNullValue()));

        // Document
        actions
            .andDo(document("error-example",
                responseFields(
                    fieldWithPath("error").description("발생한 HTTP 에러 (e.g. `Not Found`)"),
                    fieldWithPath("message").description("에러의 원인 설명"),
                    fieldWithPath("path").description("요청을 보낸 경로"),
                    fieldWithPath("status").description("HTTP 상태 코드 (e.g. `404`)"),
                    fieldWithPath("timestamp").description("에러가 발생한 시각 (단위: 밀리세컨드)")
                )));
    }

}
