package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class CheckedAppTest {

    /**
     * checked exception 의존 관계에 대한 문제
     * 체크 예외는 컨트롤러나 서비스 입장에서는 본인이 처리할 수 없어도 어쩔수 없이 throws를 통해 던지는 예외를 선언을 해줘야 한다
     * 또, 서비스, 컨트롤러에서 아래와 같은 문제는 SQLException을 의존하기 때문에 문제가 된다
     * SQLException은 JDBC 기술이기 때문에 향후에 repository를 JDBC 기술이 아니라 다른 기술로 변경을 하게 된다면 ex)JPA
     * JPA 기술에 맞춰 모든 Exception을 다 바꿔야 한다
     */

    @Test
        void checked() {
            CheckedAppTest.Controller controller = new CheckedAppTest.Controller();
            assertThatThrownBy(() -> controller.request())
                    .isInstanceOf(Exception.class);
        }

        static class Controller {
            Service service = new Service();

            public void request() throws SQLException, ConnectException {
            service.logic();
        }
    }

    static class Service {

        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        /**
         * throws Exception으로 적게 되면 코드 깔끔해지는 것 같지만
         * Exception은 최상위 타입이므로 모든 체크 예외를 다 밖으로 던지는 문제가 발생한다
         * 다른 체크 예외를 체크할 수 있는 기능이 무효화 되고, 중요한 체크 예외를 다 놓치게 된다
         * 중간에 중요한 체크 예외가 발생해도 컴파일러는 Exception을 던지기 때문에 문법에 맞다고 판단해서 컴파일 오류가 발생하지 않는다
         */
        public void logic() throws SQLException, ConnectException {
            repository.call();
            networkClient.call();
        }

    }

    // checked exception
    static class NetworkClient {
        public void call() throws ConnectException {
            throw new ConnectException("연결 실패");
        }
    }

    // checked exception
    static class Repository {
        public void call() throws SQLException {
            throw new SQLException("ex");
        }
    }

}
