package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

/**
 *  JDBCTemplate 사용
 *  커넥션 동기화(불필요한 코드 제거)
 *  예외 발생시 스프링 예외 변환기도 자동으로 실행(ExceptionTranslator)
 *  자동으로 실행
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository{

    private final JdbcTemplate template;

    // 생성자(의존관계 주입)
    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }


    // 인터페이스에서 상속받는거이기 때문에 메소드에 @Override 어노테이션 적용
    // 안넣어줘도 상관없긴한데 오류가 있을시 컴파일러가 오류를 잡아줌
    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values(?, ?)";
        // save인데 template.update인 이유는 pstmt.executeUpdate()와 맞춰줄려고
        // update는 실행된 행의 수
        // JdbcTemplate에서 끝
        int update = template.update(sql, member.getMemberId(), member.getMoney());
        return member;
    }

    // 한건 조회시에는 JdbcTemplate의 queryForObject 활용
    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        return template.queryForObject(sql, memberRowMapper(), memberId);
    }


    // 수정
    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";
        template.update(sql, money, memberId);
    }

    // 삭제
    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";
        template.update(sql, memberId);
    }

    // 조회 매핑 코드
    // 람다식을 활용한 findbyid
    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }
}
