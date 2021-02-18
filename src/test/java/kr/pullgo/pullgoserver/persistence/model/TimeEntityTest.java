package kr.pullgo.pullgoserver.persistence.model;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class TimeEntityTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    void findDummy_DummySaved_CreatedDateNotChanged() {
        Dummy savedDummy = new Dummy();
        em.persist(savedDummy);
        em.flush();
        em.clear();

        Dummy loadedDummy = em.find(Dummy.class, savedDummy.getId());

        assertThat(savedDummy.getCreatedDate()).isNotNull();
        assertThat(loadedDummy.getCreatedDate())
            .isEqualTo(savedDummy.getCreatedDate());
    }

    @Test
    void findDummy_DummyModified_ModifiedDateNotChanged() {
        Dummy savedDummy = new Dummy();
        savedDummy.setContext("first context");
        em.persist(savedDummy);
        em.flush();

        savedDummy.setContext("second context");
        em.flush();
        em.clear();

        Dummy loadedDummy = em.find(Dummy.class, savedDummy.getId());

        assertThat(savedDummy.getModifiedDate()).isNotNull();
        assertThat(loadedDummy.getModifiedDate())
            .isEqualTo(savedDummy.getModifiedDate());
    }

    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    @Entity
    static class Dummy extends TimeEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String context;
    }
}