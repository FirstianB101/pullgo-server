package kr.pullgo.pullgoserver.helper;

import kr.pullgo.pullgoserver.persistence.model.Student;

public class StudentHelper {

    public static Student studentWithId(Long id) {
        Student student = Student.builder()
            .parentPhone("010-0000-1111")
            .schoolName("KwangWoon")
            .schoolYear(3)
            .build();
        student.setId(id);
        return student;
    }
}
