package kr.pullgo.pullgoserver.service;

import kr.pullgo.pullgoserver.config.security.UserPrincipal;
import kr.pullgo.pullgoserver.dto.AuthDto;
import kr.pullgo.pullgoserver.dto.StudentDto;
import kr.pullgo.pullgoserver.dto.TeacherDto;
import kr.pullgo.pullgoserver.dto.mapper.StudentDtoMapper;
import kr.pullgo.pullgoserver.dto.mapper.TeacherDtoMapper;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import kr.pullgo.pullgoserver.persistence.repository.AccountRepository;
import kr.pullgo.pullgoserver.persistence.repository.StudentRepository;
import kr.pullgo.pullgoserver.persistence.repository.TeacherRepository;
import kr.pullgo.pullgoserver.service.helper.ServiceErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final StudentRepository studentRepository;
    private final StudentDtoMapper studentDtoMapper;
    private final TeacherRepository teacherRepository;
    private final TeacherDtoMapper teacherDtoMapper;
    private final ServiceErrorHelper errorHelper;

    @Autowired
    public AuthTokenService(JwtService jwtService,
        PasswordEncoder passwordEncoder,
        AccountRepository accountRepository,
        StudentRepository studentRepository,
        StudentDtoMapper studentDtoMapper,
        TeacherRepository teacherRepository,
        TeacherDtoMapper teacherDtoMapper,
        ServiceErrorHelper errorHelper) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.studentRepository = studentRepository;
        this.studentDtoMapper = studentDtoMapper;
        this.teacherRepository = teacherRepository;
        this.teacherDtoMapper = teacherDtoMapper;
        this.errorHelper = errorHelper;
    }

    public AuthDto.GenerateTokenResult generateToken(AuthDto.GenerateToken dto) {
        Account account = accountRepository.findByUsername(dto.getUsername())
            .orElseThrow(() -> errorHelper.unauthorized("Username not found"));

        if (!passwordEncoder.matches(dto.getPassword(), account.getPassword())) {
            throw errorHelper.unauthorized("Wrong password");
        }

        String token = jwtService.signJwt(account);

        return AuthDto.GenerateTokenResult.builder()
            .token(token)
            .student(getStudentDtoIfExists(account.getId()))
            .teacher(getTeacherDtoIfExists(account.getId()))
            .build();
    }

    public AuthDto.MeResult getMe(Authentication authentication) {
        if (authentication == null)
            throw errorHelper.unauthorized("Not authenticated");

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        Account account = accountRepository.findById(principal.getAccountId())
            .orElseThrow(() -> errorHelper.unauthorized("Removed account"));

        return AuthDto.MeResult.builder()
            .student(getStudentDtoIfExists(account.getId()))
            .teacher(getTeacherDtoIfExists(account.getId()))
            .build();
    }

    private StudentDto.Result getStudentDtoIfExists(Long accountId) {
        Student student = studentRepository.findByAccountId(accountId);
        if (student != null) {
            return studentDtoMapper.asResultDto(student);
        }
        return null;
    }

    private TeacherDto.Result getTeacherDtoIfExists(Long accountId) {
        Teacher teacher = teacherRepository.findByAccountId(accountId);
        if (teacher != null) {
            return teacherDtoMapper.asResultDto(teacher);
        }
        return null;
    }

}
