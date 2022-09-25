package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class CheckedTest {

    @Test
    void checked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_throw() {
        Service service = new Service();
        // service.callThrow() 메소드를 호출하면 MyCheckedException이 터져야 정상실행코드
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyCheckedException.class);
    }

    /**
     * Exception을 상속받은 예외는 체크 예외가 된다
     */
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            super(message);
        }
    }

    /**
     * Checked 예외는
     * 예외를 잡아서 처리하거나, 던지거나 둘중 하나를 필수로 선택해야 한다
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         */
        public void callCatch() {
            try {
                repository.call(); // checked 예외를 던진 메소드를 호출할 경우 해당 예외를 처리
            } catch (MyCheckedException e) {
                // 예외 처리 로직
                // 로그 출력 메세지 이후 Exception에 대한 정보가 출력이 되는데 마지막 파라미터 (e)에 대한 정보
                log.info("예외 처리, message = {}", e.getMessage(), e);
            }
        }

        /**
         * 체크 예외를 밖으로 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws 예외를 메소드에 필수로 선언해야한다.
         * @throws MyCheckedException
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository {
        // checked exception은 무조건 잡거나 던지거나 둘중 하나를 선언을 해줘야 한다
        // 잡거나 밖으로 던지는걸 안하게 될경우 컴파일시 오류 발생(컴파일러가 체크를 해주는거임)
        public void call() throws MyCheckedException {
            throw new MyCheckedException("ex");
        }
    }

}
