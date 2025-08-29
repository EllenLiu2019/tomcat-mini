package app2;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class TestServlet extends HttpServlet {

    //static int count = 0;

    String doc = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="utf-8"><title>Test</title></head>
                <body color="#f0f0f0">
                <h1 align="center"> v2 Test 你好 </h1>
                """;
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("v2 doGet");
        System.out.println("v2 parameter name : " + request.getParameter("name"));

        //count++;
        //System.out.println("---------- call count --------- " + TestServlet.count);
        //if (count > 2) {
        response.addHeader("Connection", "close");
        //}

        HttpSession session = request.getSession(true);
        String user = (String) session.getAttribute("user");
        System.out.println("v2 get user from session : " + user);
        if (user == null || user.isEmpty()) {
            session.setAttribute("user", "testUser");
        }

        System.out.println(doc);

        response.getWriter().println(doc);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("v2 doPost");
        System.out.println("v2 parameter name : " + request.getParameter("name"));

        System.out.println(doc);

        response.getWriter().println(doc);
    }

}
