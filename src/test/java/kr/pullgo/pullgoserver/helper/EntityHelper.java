package kr.pullgo.pullgoserver.helper;

import static kr.pullgo.pullgoserver.helper.AcademyHelper.anAcademy;
import static kr.pullgo.pullgoserver.helper.AccountHelper.anAccount;
import static kr.pullgo.pullgoserver.helper.AttenderAnswerHelper.anAttenderAnswer;
import static kr.pullgo.pullgoserver.helper.AttenderStateHelper.anAttenderState;
import static kr.pullgo.pullgoserver.helper.ClassroomHelper.aClassroom;
import static kr.pullgo.pullgoserver.helper.ExamHelper.anExam;
import static kr.pullgo.pullgoserver.helper.LessonHelper.aLesson;
import static kr.pullgo.pullgoserver.helper.QuestionHelper.aQuestion;
import static kr.pullgo.pullgoserver.helper.ScheduleHelper.aSchedule;
import static kr.pullgo.pullgoserver.helper.StudentHelper.aStudent;
import static kr.pullgo.pullgoserver.helper.TeacherHelper.aTeacher;

import java.util.HashSet;
import java.util.function.Function;
import javax.persistence.EntityManager;
import kr.pullgo.pullgoserver.persistence.model.Academy;
import kr.pullgo.pullgoserver.persistence.model.Account;
import kr.pullgo.pullgoserver.persistence.model.AttenderAnswer;
import kr.pullgo.pullgoserver.persistence.model.AttenderState;
import kr.pullgo.pullgoserver.persistence.model.Classroom;
import kr.pullgo.pullgoserver.persistence.model.Exam;
import kr.pullgo.pullgoserver.persistence.model.Lesson;
import kr.pullgo.pullgoserver.persistence.model.Question;
import kr.pullgo.pullgoserver.persistence.model.Schedule;
import kr.pullgo.pullgoserver.persistence.model.Student;
import kr.pullgo.pullgoserver.persistence.model.Teacher;
import org.springframework.stereotype.Component;

@Component
public class EntityHelper {

    private final EntityManager em;

    public EntityHelper(EntityManager em) {
        this.em = em;
    }

    public Account generateAccount() {
        return generateAccount(noInit());
    }

    public Account generateAccount(Function<? super Account, ? extends Account> initialize) {
        Account account = anAccount().withId(null);

        account = initialize.apply(account);
        em.persist(account);

        return account;
    }

    public Student generateStudent() {
        return generateStudent(noInit());
    }

    public Student generateStudent(Function<? super Student, ? extends Student> initialize) {
        Student student = aStudent()
            .withId(null)
            .withAccount(null);

        student = initialize.apply(student);
        if (student.getAccount() == null) {
            student.setAccount(generateAccount());
        }

        em.persist(student);
        return student;
    }

    public Teacher generateTeacher() {
        return generateTeacher(noInit());
    }

    public Teacher generateTeacher(Function<? super Teacher, ? extends Teacher> initialize) {
        Teacher teacher = aTeacher()
            .withId(null)
            .withAccount(null);

        teacher = initialize.apply(teacher);
        if (teacher.getAccount() == null) {
            teacher.setAccount(generateAccount());
        }

        em.persist(teacher);
        return teacher;
    }

    public Academy generateAcademy() {
        return generateAcademy(noInit());
    }

    public Academy generateAcademy(Function<? super Academy, ? extends Academy> initialize) {
        Academy academy = anAcademy()
            .withId(null)
            .withTeachers(new HashSet<>())
            .withOwner(null);

        academy = initialize.apply(academy);
        if (academy.getOwner() == null) {
            Teacher owner = generateTeacher();
            academy.addTeacher(owner);
            academy.setOwner(owner);
        }

        em.persist(academy);
        return academy;
    }

    public Classroom generateClassroom() {
        return generateClassroom(noInit());
    }

    public Classroom generateClassroom(
        Function<? super Classroom, ? extends Classroom> initialize) {
        Classroom classroom = aClassroom()
            .withId(null)
            .withCreator(null)
            .withAcademy(null);

        classroom = initialize.apply(classroom);
        if (classroom.getAcademy() == null) {
            classroom.setAcademy(generateAcademy());
        }
        if (classroom.getCreator() == null) {
            Teacher creator = generateTeacher();
            classroom.setCreator(creator);
            classroom.addTeacher(creator);
        } else {
            classroom.addTeacher(classroom.getCreator());
        }

        em.persist(classroom);
        return classroom;
    }

    public Schedule generateSchedule() {
        return generateSchedule(noInit());
    }

    public Schedule generateSchedule(Function<? super Schedule, ? extends Schedule> initialize) {
        Schedule schedule = aSchedule().withId(null);

        schedule = initialize.apply(schedule);

        em.persist(schedule);
        return schedule;
    }

    public Lesson generateLesson() {
        return generateLesson(noInit());
    }

    public Lesson generateLesson(Function<? super Lesson, ? extends Lesson> initialize) {
        Lesson lesson = aLesson()
            .withId(null)
            .withClassroom(null)
            .withSchedule(null);

        lesson = initialize.apply(lesson);
        if (lesson.getClassroom() == null) {
            lesson.setClassroom(generateClassroom());
        }
        if (lesson.getSchedule() == null) {
            lesson.setSchedule(generateSchedule());
        }

        em.persist(lesson);
        return lesson;
    }

    public Exam generateExam() {
        return generateExam(noInit());
    }

    public Exam generateExam(Function<? super Exam, ? extends Exam> initialize) {
        Exam exam = anExam()
            .withId(null)
            .withClassroom(null)
            .withCreator(null);

        exam = initialize.apply(exam);
        if (exam.getClassroom() == null) {
            exam.setClassroom(generateClassroom());
        }
        if (exam.getCreator() == null) {
            exam.setCreator(generateTeacher());
        }

        em.persist(exam);
        return exam;
    }

    public Question generateQuestion() {
        return generateQuestion(noInit());
    }

    public Question generateQuestion(Function<? super Question, ? extends Question> initialize) {
        Question question = aQuestion()
            .withId(null)
            .withExam(null);

        question = initialize.apply(question);
        if (question.getExam() == null) {
            question.setExam(generateExam());
        }

        em.persist(question);
        return question;
    }

    public AttenderState generateAttenderState() {
        return generateAttenderState(noInit());
    }

    public AttenderState generateAttenderState(
        Function<? super AttenderState, ? extends AttenderState> initialize) {
        AttenderState attenderState = anAttenderState()
            .withId(null)
            .withAttender(null)
            .withExam(null);

        attenderState = initialize.apply(attenderState);
        if (attenderState.getAttender() == null) {
            attenderState.setAttender(generateStudent());
        }
        if (attenderState.getExam() == null) {
            attenderState.setExam(generateExam());
        }

        em.persist(attenderState);
        return attenderState;
    }

    public AttenderAnswer generateAttenderAnswer() {
        return generateAttenderAnswer(noInit());
    }

    public AttenderAnswer generateAttenderAnswer(
        Function<? super AttenderAnswer, ? extends AttenderAnswer> initialize) {
        AttenderAnswer attenderAnswer = anAttenderAnswer()
            .withId(null)
            .withAttenderState(null)
            .withQuestion(null);

        attenderAnswer = initialize.apply(attenderAnswer);
        if (attenderAnswer.getAttenderState() == null) {
            attenderAnswer.setAttenderState(generateAttenderState());
        }
        if (attenderAnswer.getQuestion() == null) {
            attenderAnswer.setQuestion(generateQuestion());
        }

        em.persist(attenderAnswer);
        return attenderAnswer;
    }

    private <T> Function<? super T, ? extends T> noInit() {
        return (arg) -> arg;
    }

}
