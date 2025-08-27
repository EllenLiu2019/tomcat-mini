package server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class TestServlet extends HttpServlet {

    String doc = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="utf-8"><title>Test</title></head>
                <body color="#f0f0f0">
                <h1 align="center"> Test 你好 </h1>
                """;
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Enter doGet()");
        System.out.println("parameter name : " + request.getParameter("name"));

        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(true);
        String user = (String) session.getAttribute("user");
        System.out.println("get user from session : " + user);
        if (user == null || user.isEmpty()) {
            session.setAttribute("user", "testUser");
        }

        System.out.println(doc);

        response.getWriter().println(doc);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Enter doGet()");
        System.out.println("parameter name : " + request.getParameter("name"));

        response.setCharacterEncoding("UTF-8");

        System.out.println(doc);

        response.getWriter().println(doc);
    }

}
