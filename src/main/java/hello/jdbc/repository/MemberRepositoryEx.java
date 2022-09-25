package hello.jdbc.repository;

import hello.jdbc.domain.Member;

import java.sql.SQLException;

public interface MemberRepositoryEx {

    /**
     * checked interface
     * Exception을 의존관계로 쓰기 싫어서 인터페이스를 만들었는데
     * checked exception은 인터페이스에 throws를 적어주고 구현체에도 똑같이 적용해야됨(문제가 2배)
     */

    Member save(Member member) throws SQLException;
    Member findById(String memberId) throws SQLException;
    void update(String memberId, int money) throws SQLException;
    void delete(String memberId) throws SQLException;

}
