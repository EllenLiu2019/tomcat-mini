package lib;


import com.ellen.tomcat.mini.ContainerEvent;
import com.ellen.tomcat.mini.ContainerListener;

public class HostListener implements ContainerListener {

    @Override
    public void containerEvent(ContainerEvent event) {
        System.out.println("publish host event: "  + event);
    }

}
