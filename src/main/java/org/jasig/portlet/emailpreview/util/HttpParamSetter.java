package org.jasig.portlet.emailpreview.util;

import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import org.apache.http.client.HttpClient;
import org.apache.http.params.HttpParams;

/**
 * HttpParamSetter is a utility bean that allows you to set parameters on an HttpClient client.  Since HttpClient.setParameters()
 * requires two arguments, it cannot be set using typical bean setters.  This bean is configured with parameter values, passed
 * in as a map.  Once the httpClient is fully built, using default values, the parameters are added.
 * 
 * @author mgillian
 *
 */
public class HttpParamSetter {

    private HttpClient httpClient;
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private Map<String, Object> parameters;
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @PostConstruct
    public void applyParameters() {
    	HttpParams httpParams = httpClient.getParams();
        for(Entry<String, Object> entry:parameters.entrySet()){
            httpParams.setParameter(entry.getKey(), entry.getValue());
        }
    }
}