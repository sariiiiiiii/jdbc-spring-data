package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - DataSource, transactionManager 자동등록
 */
@Slf4j
// @SpringBootTest 어노테이션이 있으면 테스트시 스프링부트를 통해 스프링 컨테이너를 생성한다. 그리고 테스트에서 @Autowired 등을 통해 스프링 컨테이너가 관리하는 빈들을 사용할 수 있다
@SpringBootTest
class MemberServiceV3_4Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepositoryV3 memberRepository;
    @Autowired
    private MemberServiceV3_3 memberService; // 의존관계 주입시 실제 MemberService가 아니라 오버라이딩 된 프록시 트랜잭션이 주입됨

    @TestConfiguration
    // 테스트클래스 안에서 내부 설정 클래스를 안에서 묻히면 스프링부트가 자동으로 만들어주는 빈들에 추가로 필요한 스프링 빈들을 만들고 사용할 수 있다 
    static class TestConfig {

        private final DataSource dataSource;

        // application.properties에 지정된 속성을 참고해서 데이터소스와 트랜잭션 매니저를 자동으로 생성
        // 스프링부트가 만들어준 dataSource 빈을 주입
        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource);
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }

    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    void AopCheck() {

        log.info("memberService class={}", memberService.getClass());
        // 트랜잭션 처리 로직이 실제 서비스를 호출할 때 상속을 받는다(오버라이딩)
        // 그래서 실행되는 service는 실제 MemberSerivce가 아니라 트랜잭션 프록시 코드이다
        // memberService class=class hello.jdbc.service.MemberServiceV3_3$$EnhancerBySpringCGLIB$$3ae9b06f (CGLIB) = 프록시

        log.info("memberRepository class={}", memberRepository.getClass()); // 정상 repository

        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue(); // proxy = true
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse(); // proxy = false
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {

        //given = 이렇게 데이터가 준비가 되어 있을 떄
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when = 이걸 수행하면\
        log.info("START TX");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("END TX");

        //then = 이렇게 되야 된다
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);

    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {

        //given = 이렇게 데이터가 준비가 되어 있을 떄
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEX = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEX);

        //when = 이걸 수행하면
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then = 이렇게 되야 된다
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEX.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);

    }

}
