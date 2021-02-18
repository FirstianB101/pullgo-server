package kr.pullgo.pullgoserver.helper;

import kr.pullgo.pullgoserver.persistence.model.Teacher;

public class TeacherHelper {

    public static Teacher teacherWithId(Long id) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        return teacher;
    }
}
