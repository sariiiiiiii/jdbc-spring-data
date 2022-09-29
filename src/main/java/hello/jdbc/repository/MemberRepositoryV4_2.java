package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 *  SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository{

    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;

    // 생성자(의존관계 주입)
    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;

        // SQLExceptionTranslator 인터페이스에서 SQLErrorCodeSQLExceptionTranslator 구현체를 사용
        // datasource를 넣어주는 이유는 어떤 DB를 쓰는지 찾아서 꺼내기 쓰기 때문
        // 최상위는 DataAccessException
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }


    // 인터페이스에서 상속받는거이기 때문에 메소드에 @Override 어노테이션 적용
    // 안넣어줘도 상관없긴한데 오류가 있을시 컴파일러가 오류를 잡아줌
    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate(); // 위 준비된게 db 실행
            return member;
        } catch (SQLException e) {
            throw exTranslator.translate("save", sql, e);
//            throw new MyDbException(e);
        } finally {
            close(conn, pstmt, null);

        }
    }

    // 조회
    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null; // db 반환값 담을 객체

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId); // errorException은 키값을 넣어주는것이 중요
            }

        } catch (SQLException e) {
            throw exTranslator.translate("findById", sql, e);
        } finally {
            close(con, pstmt, rs);
        }
    }

    // 수정
    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize = {}", resultSize);
        } catch (SQLException e) {
            throw exTranslator.translate("update", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    // 삭제
    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw exTranslator.translate("delete", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    // 자원 종료
    private void close(Connection conn, Statement stmt, ResultSet rs) {

        // 자원종료
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        DataSourceUtils.releaseConnection(conn, dataSource);

    }

    // 드라이버 연결
    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다
        // 트랜잭션 동기화 매니저가 관리하는 커넥션이 있으면 해당 커넥션을 반환한다
        // 트랜잭션이 필요없는 경우 생성을 안한 상태에서 repository에서 접근하게 되면 트랜잭션동기화 매니저에서 새로운 커넥션을 생성해서 반환한다
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}. class={}", con, con.getClass());
         return con;
    }
}
