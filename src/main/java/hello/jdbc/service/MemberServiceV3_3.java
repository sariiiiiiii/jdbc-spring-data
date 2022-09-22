package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - @Transactional AOP
 */
@Slf4j
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    // 생성자
    public MemberServiceV3_3(MemberRepositoryV3 memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 이 메소드가 호출이 될 때 트랜잭션을 생성하겠다, 메소드가 종료될 때 성공하면 commit, runtime error가 뜨면 rollback을 해주겠다
    // @Transactional은 class 레벨에도 붙일 수 있는데 외부에서 호출 가능한 모든 public 메소드에 트랜잭션이 적용된다
    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId, toId, money);
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
