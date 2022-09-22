package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

//    private final DataSource dataSource; JDBC 코드 날리기
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        
//        Connection con = dataSource.getConnection(); JDBC 코드

        // 트랜잭션 시작
        // 현재 트랜잭션에 대한 상태정보가 포함되어 있다
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            // 트랜잭션 시작, 오토커밋 제외
//            con.setAutoCommit(false);

            // 비즈니스 로직
            bizLogic(fromId, toId, money);

//            con.commit(); // 성공 시 커밋 JDBC 코드

            // 커밋
            transactionManager.commit(status);
            
        } catch(Exception e) {
//            con.rollback(); // 실패 시 롤백 JDBC코드

            // 롤백
            transactionManager.rollback(status);

            throw new IllegalStateException(e);
        }
        // transacntionManager가 commit이나 rollback 될 때, 매니저가 알아서 release 시켜주기 때문에 이제 쓸 필요가 없다 !!! 좋다 !!
        // 매니저 내부에서 알아서 해줌
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
