package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

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
        if (true) {
            throw new IllegalStateException("....");
        }

        // delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class); // NoSuchElementException이 터지는지 확인

    }
}