package dockmind.demo.exception;

public class ResourceNotFound extends RuntimeException {

    public ResourceNotFound(){
        super("resoure you're looking for not found");
    }


    public ResourceNotFound(String msg){
        super(msg);
    }

    public ResourceNotFound(String msg , Throwable ex){
        super(msg , ex);
    }


}
