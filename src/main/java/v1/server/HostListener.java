package v1.server;


import com.ellen.tomcat.mini.ContainerEvent;
import com.ellen.tomcat.mini.ContainerListener;


public class HostListener implements ContainerListener {

    @Override
    public void containerEvent(ContainerEvent event) {
        System.out.println("v1 publish host event: "  + event.getType());
    }

}
