package kz.skills.elearning.dto;

public class MessageResponse {

    private String message;

    public static MessageResponse of(String message) {
        MessageResponse response = new MessageResponse();
        response.setMessage(message);
        return response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
