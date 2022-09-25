package hello.jdbc.repository.ex;

public class MyDbException extends RuntimeException {

    /**
     * RuntimeException을 상속받았기 때문에 unchecked exception
     * 기존예외 로그 출력을 위한 Throwable cause를 매개변수로 받는 생성자 생성
     */

    public MyDbException() {
    }

    public MyDbException(String message) {
        super(message);
    }

    public MyDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDbException(Throwable cause) {
        super(cause);
    }
}
