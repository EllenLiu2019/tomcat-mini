package v1.server;


import com.ellen.tomcat.mini.ContainerEvent;
import com.ellen.tomcat.mini.ContainerListener;


public class TestListener implements ContainerListener {

    @Override
    public void containerEvent(ContainerEvent event) {
        System.out.println(event);
    }

}
