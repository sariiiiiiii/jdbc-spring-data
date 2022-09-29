package hello.jdbc.exception.translator;

import hello.jdbc.connection.ConnectionConst;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class SpringExceptionTranslatorTest {

    DataSource dataSource;

    @BeforeEach
    void init() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    /**
     * 문제점
     * 이전에 살펴봤던 SQL ErrorCode를 직접 확인하는 방법이다.
     * 직접 예외를 확인하고 하나하나 스프링이 만들어준 예외로 변환하는 것은 현실성이 없다.
     * 이렇게 하려면 해당 오류 코드를 확인하고 스프링의 예외 체계에 맞추어 예외를 직접 변환해야 한다
     * 그리고 데이터베이스마다 오류 코드가 다르다는 점도 해결해야 한다.
     * 그래서 스프링은 예외변환기를 이용한다
     */

    /**
     * 그럼 스프링에서는 어떻게 DB마다 다른 Errorcode를 인식해서 각각의 맞는 Exception을 반환해줄까?
     * org.springframework.jdbc.support.sql-error-codes.xml
     * 스프링 SQL 예외 변환기는 SQL ErrorCode를 이 파일에 대입해서 어떤 스프링 데이터 접근 예외로 전환해야 할지 찾아낸다
     * 예를 들어서 H2 데이터베이스에서 "42000"이 발생하면 badSqlGrammerCodes이기 때문에 BadSqlGrammarException을 반환한다
     * IntelliJ -> double shift -> sql-error-codes.xml 검색하면 나옴
     */
    
    @Test
    void sqlExceptionErrorCode() {
        String sql = "select bad grammer";

        try {
            Connection con = dataSource.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.executeQuery();
        } catch (SQLException e) {
            Assertions.assertThat(e.getErrorCode()).isEqualTo(42122);
//            throw new BadSqlGrammarException(e);
            int errorCode = e.getErrorCode();
            log.info("errorCode={}", errorCode);
            log.info("error", e);
        }
    }

    @Test
    void exceptionTranslator() {
        String sql = "select bad grammer";

        try {
            Connection con = dataSource.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.executeQuery();
        } catch (SQLException e) {
            Assertions.assertThat(e.getErrorCode()).isEqualTo(42122);
            SQLErrorCodeSQLExceptionTranslator exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);

            // BadSqlGrammarException
            // 스프링 예외 추상화
            // 예외 변환을 해주는 코드를 직접 짤 필요 없이 스프링에서 제공하는 데이터 접근예외 사용
            // translate의 첫 번째 파라미터는 읽을 수 있는 설명(select), 두 번째는 실행한 sql, 마지막은 발생된 SQLException을 전달하면 적절한 스프링 데이터 접근 계층의 예외로 변환해서 반환
            DataAccessException resultEx = exTranslator.translate("select", sql, e);
            log.info("resultEx", resultEx);
            Assertions.assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class); // 스프링 데이터 접근 계층의 예외로 변환해서 반환(BadSqlGrammerException이 반환된 것을 볼 수 있음)
        }
    }

}
