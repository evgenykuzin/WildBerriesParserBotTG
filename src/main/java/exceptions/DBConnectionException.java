package exceptions;

public class DBConnectionException extends Exception {
    @Override
    public String getMessage() {
        return "lost database connection";
    }
}
