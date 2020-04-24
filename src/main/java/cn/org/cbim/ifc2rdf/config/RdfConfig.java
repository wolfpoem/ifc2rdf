package cn.org.cbim.ifc2rdf.config;

import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RdfConfig {
    @Value("${app.jena-fusiki.host}")
    private String host;

    @Value("${app.jena-fusiki.port}")
    private String port;

    @Bean
    public RDFConnection rdfConnection(){
        RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
                .destination("http://"+host+":"+port+"/ds/")
                .queryEndpoint("query")
                .updateEndpoint("update")
                .gspEndpoint("data");
        return builder.build();
    }
}
