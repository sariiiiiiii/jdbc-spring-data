package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    @BeforeEach // 각 테스트가 실행되기 직전에 실행
    void beforeEach() {

        // 기본 DriverManager - 항상 새로운 커넥션을 획득
        // query문을 하나씩 실행시킬때마다 connection을 계속 생성한다(성능 저하)
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // 커넥션 풀링
        // 커넥션이 자원종료 close() 하게 되면 커넥션풀인 경우에는 닫는게 아니라 커넥션을 반환하게 된다
        // 커넥션 0번을 썻다가 반환하고 0번을 썼다가 반환한다
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new MemberRepositoryV1(dataSource);

        /**
         * DriverManagerDataSource를 HikariDataSource로 변경해도 MemberRepositoryV1Test의 코드는 변환되지 않는다 why?
         * DrivermanagerDataSource와 KihariDataSource는 DataSource를 의존하기 때문 !!
         * 이것이 DataSource를 사용하는 이유이자 장점 !!
         */

    }


    @Test
    void crud() throws SQLException {

        // save
        Member member = new Member("memberV100", 10000);
        repository.save(member);

        // select
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember = {}", findMember);
        log.info("member != findMember {}", member == findMember); // false
        // domain 클래스에 @Data EqualsAndHashCode 공부
        log.info("member equals findMember {}", member.equals(findMember)); // true
        assertThat(findMember).isEqualTo(member);


        // update : money: 10000 => 20000
        repository.update(member.getMemberId(), 20000);
        Member updateMember = repository.findById(member.getMemberId());
        assertThat(updateMember.getMoney()).isEqualTo(20000);

        // 만약 중간로직에 excepion이 터진다면 exception 밑 로직은 실행이 되지 않기때문에 엉킬수가 있다 (주의 **)
//        if (true) {
//            throw new IllegalStateException("....");
//        }

        // delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class); // NoSuchElementException이 터지는지 확인

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}