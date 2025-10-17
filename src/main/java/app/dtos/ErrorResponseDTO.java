package app.dtos;

public class ErrorResponseDTO {

    private String error;
    private String message;
    private String path;
    private String method;

    public ErrorResponseDTO(String error, String message, String path, String method){
        this.error = error;
        this.message = message;
        this.path = path;
        this.method = method;
    }
}
