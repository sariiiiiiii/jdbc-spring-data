package hello.jdbc.connection;

public abstract class ConnectionConst {

    // 상수를 모아서 쓴거기 때문에 객체 생성을 하면 안되서 abstract로 객체 생성 방지

    public static final String URL = "jdbc:h2:tcp://localhost/~/test";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";

}
