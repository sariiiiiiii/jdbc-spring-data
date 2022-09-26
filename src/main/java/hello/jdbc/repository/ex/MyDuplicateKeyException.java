package hello.jdbc.repository.ex;

public class MyDuplicateKeyException extends MyDbException {

    /**
     * DB에 대한 오류가 때문에 MyDbException을 상속
     * 중복값 exception
     */

    public MyDuplicateKeyException() {
    }

    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
