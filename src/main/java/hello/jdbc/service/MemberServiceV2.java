package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;
    
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        
        // 커넥션 생성
        Connection con = dataSource.getConnection();

        try {
            // 트랜잭션 시작, 오토커밋 제외
            con.setAutoCommit(false);

            // 비즈니스 로직
            bizLogic(con, fromId, toId, money);
            con.commit(); // 성공 시 커밋
            
        } catch(Exception e) {
            con.rollback(); // 실패 시 롤백
            throw new IllegalStateException(e);
        } finally {
            release(con); // 커넥션 종료 메서드로 이동
        }
    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private void release(Connection con) {
        if (con != null) {
            try {
                // 커넥션은 사용 시 반납을 하는데 setAutoCommit을 false인 상태로 반납을 하게 되면
                // 누군가가 커넥션을 사용할 때 setAutoCommit(false)인 상태로 커넥션을 받기 때문에 true로 바꿔주자
                con.setAutoCommit(true); // 커넥션 풀 고려
                con.close();
            } catch (Exception e) {
                // error를 log로 남길때는 = {}을 사용하지 않는다
                log.info("error", e);
            }
        }
    }

    private void validation(Member member) {
        if (member.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
