package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws SQLException {
        // DriverManagerDataSource - 항상 새로운 커넥션을 획득
        // spring에서 제공하는 Drivermanager

        // DriverManager.getConnection은 URL, USERNAME, PASSWORD를 계속 넣어 사용할때마다 세팅
        // new DriverManagerDataSource는 생성시에 한번 세팅해주고 사용할 때는 getConnection만 호출하여 사용(설정과 사용을 분리)

        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD); // 설정(초기 정보 입력)
        useDataSource(dataSource); // 사용(설정을 신경쓰지 않고 해당 메소드를 호출하여 사용)
    }

    @Test // 커넥션 풀
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        // 커넥션 풀링
        // Spring JDBC를 사용하면 기본적으로 있음
        HikariDataSource dataSource = new HikariDataSource(); // DataSource Interface 구현
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10); // default 10 커넥션 최대 풀 사이즈
        dataSource.setPoolName("MyPool");

        useDataSource(dataSource);
        // 커넥션 풀에서 커넥션을 생성하는 작업은 애플리케이션 실행속도에 영향을 주지 않기 위해
        // 별도의 쓰레드에서 작동한다 로그를 보면 Added connection conn1: dataSource 정보
        // MyPool connection adder (별도의 쓰레드)
        Thread.sleep(1000);
    }

    @Test
    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection(); // 커넥션이 반환된 것이 없다면 약간의 대기시간을 가져 풀을 획득한다 (10개까지는 이렇게 동작)
        Connection con3 = dataSource.getConnection();
        Connection con4 = dataSource.getConnection();
        Connection con5 = dataSource.getConnection();
        Connection con6 = dataSource.getConnection();
        Connection con7 = dataSource.getConnection();
        Connection con8 = dataSource.getConnection();
        Connection con9 = dataSource.getConnection();
        Connection con10 = dataSource.getConnection();
        Connection con11 = dataSource.getConnection();
        Connection con12 = dataSource.getConnection();
        // 17:39:39.991 [MyPool housekeeper] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Pool stats (total=10, active=10, idle=0, waiting=1)
        // [MyPool housekeeper] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Fill pool skipped, pool is at sufficient level.
        // setMaximumPoolSize가 10으로 설정되어있는데 Connection을 12개 할당하니까 계속 대기시간이 돌아가다가 현재 failed됨
        // pool 확보가 될때까지 block(대기)를 갖고, 얼마나 대기시간을 가질건지 예외를 터트릴건지 설정이 있음 (보통 짧게 갖는다 길게 가지면 애플리케이션 속도가 느려짐)
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

}
