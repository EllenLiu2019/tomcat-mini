package app2;


import com.ellen.tomcat.mini.ContainerEvent;
import com.ellen.tomcat.mini.ContainerListener;


public class ContextListener implements ContainerListener {

    @Override
    public void containerEvent(ContainerEvent event) {
        System.out.println("v2 publish context event: "  + event);
    }

}
