package com.ellen.tomcat.mini.core;

import com.ellen.tomcat.mini.util.URLDecoder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class FilterMap {

    @Getter
    @Setter
    private String filterName;

    @Getter
    @Setter
    private String servletName;

    @Getter
    private String urlPattern;


    public void setURLPattern(String urlPattern) {
        this.urlPattern = URLDecoder.URLDecode(urlPattern);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("FilterMap[");
        sb.append("filterName=");
        sb.append(this.filterName);
        if (servletName != null) {
            sb.append(", servletName=");
            sb.append(servletName);
        }
        if (urlPattern != null) {
            sb.append(", urlPattern=");
            sb.append(urlPattern);
        }
        sb.append("]");
        return (sb.toString());
    }
}
