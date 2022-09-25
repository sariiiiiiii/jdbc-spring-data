package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UnCheckedAppTest {

    /**
     * CheckedAppTest 클래스 코드를 Unchecked로 변경
     * throws SQLException과 ConnectException을 runtimeException으로 변경
     * repository에서 runtimeException을 넘겨주기 때문에 throws Exception 지저분한 부분을 없어짐
     * 의존하는 의존관계도 사라짐
     */

    @Test
        void Unchecked() {
            UnCheckedAppTest.Controller controller = new UnCheckedAppTest.Controller();
            assertThatThrownBy(() -> controller.request())
                    .isInstanceOf(RuntimeException.class);
        }

        static class Controller {
            Service service = new Service();

            public void request() {
            service.logic();
        }
    }

    /**
     * 마지막 파라미터에 예외를 넣어주면 로그에 스택 트레이스를 출력할 수 있다
     */
    @Test
    void printEx() {
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
//            e.printStackTrace(); 좋지 않음
            log.info("ex", e);
        }
    }

    static class Service {

        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            repository.call();
            networkClient.call();
        }

    }

    static class NetworkClient {
        public void call() {
            throw new RuntimeConnectException("연결 실패");
        }
    }

    // Repository가 checked exception을 try ~ catch로 잡고 밖으로 던질 떄는 Runtime 예외로 던짐
    static class Repository {
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
//                throw new RuntimeSQLException(); 파라미터로 SQLException을 안 넘겨주게 될 경우 무엇 때문에 예외전환을 해줬는제 알 수가 없다 **주의
                throw new RuntimeSQLException(e);
            }
        }

        public void runSQL () throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        // 생성자중에 Throwable cause를 매개변수로 받는 생성자가 있는데 **주의
        // 왜 발생했는지 이전예외를 같이 넣을 수 있다(무엇 떄문에 RuntimeSQLException이 발생했는지 알 수가 없다)
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }

}
