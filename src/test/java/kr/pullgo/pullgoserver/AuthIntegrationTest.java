package kr.pullgo.pullgoserver;

import static kr.pullgo.pullgoserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademyCreateDto;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.util.function.Function;
import javax.sql.DataSource;
import kr.pullgo.pullgoserver.config.security.UserPrincipal;
import kr.pullgo.pullgoserver.dto.AcademyDto;
import kr.pullgo.pullgoserver.dto.AuthDto;
import kr.pullgo.pullgoserver.helper.EntityHelper;
import kr.pullgo.pullgoserver.helper.Struct;
import kr.pullgo.pullgoserver.helper.TransactionHelper;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.service.JwtService;
import kr.pullgo.pullgoserver.util.H2DbCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AuthIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_STUDENT = subsectionWithPath("student")
        .type("Student").description("계정을 소유한 학생 (학생 계정이 아니면 `null`)");
    private static final FieldDescriptor DOC_FIELD_TEACHER = subsectionWithPath("teacher")
        .type("Teacher").description("계정을 소유한 선생님 (선생님 계정이 아니면 `null`)");

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .apply(basicDocumentationConfiguration(restDocumentation))
            .build();
    }

    @Test
    void generateToken() throws Exception {
        // Given
        Struct given = trxHelper.doInTransaction(() -> {
            Account account = entityHelper.generateAccount(it ->
                it.withUsername("test1234")
                    .withPassword(encodedPassword("pAsSwOrD"))
            );
            Student student = entityHelper.generateStudent(it -> it.withAccount(account));

            return new Struct()
                .withValue("accountId", account.getId())
                .withValue("studentId", student.getId());
        });
        Long accountId = given.valueOf("accountId");
        Long studentId = given.valueOf("studentId");

        // When
        AuthDto.GenerateToken dto = AuthDto.GenerateToken.builder()
            .username("test1234")
            .password("pAsSwOrD")
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/auth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        MvcResult result = actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.student.id").value(studentId))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthDto.GenerateTokenResult resultDto = fromJson(responseBody,
            AuthDto.GenerateTokenResult.class);

        UserPrincipal principal = jwtService.extractSubject(resultDto.getToken());
        assertThat(principal.getAccountId()).isEqualTo(accountId);

        // Document
        actions.andDo(document("auth-generate-token-example",
            requestFields(
                fieldWithPath("username").description("사용자 이름"),
                fieldWithPath("password").description("비밀번호")
            ),
            responseFields(
                fieldWithPath("token").description("액세스 토큰"),
                DOC_FIELD_STUDENT,
                DOC_FIELD_TEACHER
            )));
    }

    @Test
    void generateToken_InvalidPassword_UnauthorizedStatus() throws Exception {
        // Given
        trxHelper.doInTransaction(() -> {
            Account account = entityHelper.generateAccount(it ->
                it.withUsername("test1234")
                    .withPassword(encodedPassword("pAsSwOrD"))
            );
            entityHelper.generateStudent(it -> it.withAccount(account));
        });

        // When
        AuthDto.GenerateToken dto = AuthDto.GenerateToken.builder()
            .username("test1234")
            .password("wrongPassword")
            .build();
        String body = toJson(dto);

        ResultActions actions = mockMvc.perform(post("/auth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        actions
            .andExpect(status().isUnauthorized())
            .andReturn();
    }

    @Test
    void getMe() throws Exception {
        // Given
        Struct given = trxHelper.doInTransaction(() -> {
            Account account = entityHelper.generateAccount(it ->
                it.withUsername("test1234")
                    .withPassword(encodedPassword("pAsSwOrD"))
            );
            Student student = entityHelper.generateStudent(it -> it.withAccount(account));

            return new Struct()
                .withValue("token", jwtService.signJwt(account))
                .withValue("studentId", student.getId());
        });
        String token = given.valueOf("token");
        Long studentId = given.valueOf("studentId");

        // When
        ResultActions actions = mockMvc.perform(get("/auth/me")
            .header("Authorization", "Bearer " + token));

        // Then
        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.student.id").value(studentId))
            .andReturn();

        // Document
        actions.andDo(document("auth-me-example",
            responseFields(
                DOC_FIELD_STUDENT,
                DOC_FIELD_TEACHER
            )));
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    private String encodedPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Nested
    class PostAPI {

        @Test
        void postAcademy() throws Exception {

            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Teacher teacher = entityHelper.generateTeacher();
                String token = generateToken(it -> teacher.getAccount());
                return new Struct()
                    .withValue("token", token)
                    .withValue("teacherId", teacher.getId());
            });
            String token = given.valueOf("token");
            Long creatorId = given.valueOf("teacherId");

            String body = toJson(anAcademyCreateDto().withOwnerId(creatorId));

            // When
            ResultActions actions = mockMvc.perform(post("/academies")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isCreated());
        }

        @Test
        void postAcademy_InvalidCreator_ForbiddenStatus() throws Exception {

            // Given
            Long creatorId = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount(it ->
                    it.withUsername("test1234")
                        .withPassword(encodedPassword("pAsSwOrD"))
                );
                Teacher teacher = entityHelper.generateTeacher(it -> it.withAccount(account));
                return teacher.getId();
            });

            AcademyDto.Create dto = anAcademyCreateDto().withOwnerId(creatorId);
            String body = toJson(dto);

            // When
            AuthDto.GenerateToken tokenDto = AuthDto.GenerateToken.builder()
                .username("otherId")
                .password("otherPassword")
                .build();
            String wrongToken = toJson(tokenDto);

            ResultActions actions = mockMvc.perform(post("/academies")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + wrongToken)
                .content(body));

            // Then
            actions
                .andExpect(status().isForbidden())
                .andReturn();
        }

        @Test
        void postAcademy_NoToken_UnauthorizedStatus() throws Exception {

            // Given
            Long creatorId = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount(it ->
                    it.withUsername("test1234")
                        .withPassword(encodedPassword("pAsSwOrD"))
                );
                Teacher teacher = entityHelper.generateTeacher(it -> it.withAccount(account));
                return teacher.getId();
            });

            AcademyDto.Create dto = anAcademyCreateDto().withOwnerId(creatorId);
            String body = toJson(dto);

            // When
            ResultActions actions = mockMvc.perform(post("/academies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isUnauthorized())
                .andReturn();
        }


        private String generateToken(Function<? super Account, ? extends Account> initialize) {
            Account account = anAccount().withId(null);

            account = initialize.apply(account);
            return jwtService.signJwt(account);
        }
    }
}
