package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Transactional
@SpringBootTest
class QuerydslApplicationTests {

  @PersistenceContext
  EntityManager em;

  @Test
  void contextLoads() {
    Hello hello = new Hello();
    hello.setName("Hello");
    em.persist(hello);

    JPAQueryFactory query = new JPAQueryFactory(em);
    QHello qHello = QHello.hello;

    Hello hello1 = query
        .selectFrom(qHello)
        .fetchOne();

    assertThat(hello1).isEqualTo(hello);
    assert hello1 != null;
    assertThat(hello1.getId()).isEqualTo(hello.getId());
  }

}
