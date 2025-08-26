package server;

import lombok.Getter;
import lombok.Setter;

import java.io.OutputStream;


@Getter
public class Response {

    @Setter
    Request request;

    OutputStream output;

    public Response(OutputStream output) {
        this.output = output;
    }


}
