package v2.server;

import javax.servlet.*;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("v2 Filter,  authentication");

        chain.doFilter(request, response);
    }
}
