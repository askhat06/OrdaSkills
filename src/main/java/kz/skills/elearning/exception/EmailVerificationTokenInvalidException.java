package kz.skills.elearning.exception;

public class EmailVerificationTokenInvalidException extends RuntimeException {

    public EmailVerificationTokenInvalidException(String message) {
        super(message);
    }
}
