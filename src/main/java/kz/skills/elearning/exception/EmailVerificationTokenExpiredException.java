package kz.skills.elearning.exception;

public class EmailVerificationTokenExpiredException extends RuntimeException {

    public EmailVerificationTokenExpiredException(String message) {
        super(message);
    }
}
