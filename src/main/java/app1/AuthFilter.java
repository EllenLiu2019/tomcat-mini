package app1;

import javax.servlet.*;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("v1 Filter, authentication");

        chain.doFilter(request, response);
    }
}
