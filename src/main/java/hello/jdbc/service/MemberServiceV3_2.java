package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 */
@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    // 생성자
    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {

        // 트랜잭션 템플릿을 쓸려면 트랜잭션매니저가 필요해서 트랜잭션 매니저를 주입
        // MemberServiceV3_2Test에서 트랜잭션 매니저를 주입하고 있기때문에 유연성을 위해 생성자에서 트랜잭션 매니저를 주입받음
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;

    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        // 반환값이 없기 때문에 executeWithoutResult을 TransactionTemplate에서 호출
        // 트랜잭션 시작(트랜잭션 생성)
        txTemplate.executeWithoutResult((status) -> {
            try {
                // bizLogic 수행하다가 에러가 나면 SQLException을 예외로 던지는데
                // 비즈니스 로직 람다식에서는 exception을 해결할 수 없기 때문에 try ~ catch로 감싸주자
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }); // txTemplate 람다식 안에서 코드가 끝났을 때 성공적으로 끝났으면 commit, (** 언체크 예외)가 발생하면 rollback, 체크예외는 commit하는데 뒤에 강의를 더 들어보자!
    }


    // 비즈니스 로직
    private void bizLogic(String fromId, String toId, int money) throws SQLException {

        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
        
    }

    private void validation(Member member) {
        if (member.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
