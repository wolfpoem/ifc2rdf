package cn.org.cbim.ifc2rdf.service;

import org.apache.jena.rdfconnection.RDFConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SparqlService {

    @Autowired
    private RDFConnection rdfConnection;

    public Object query(String dateSet, String sql) {
        rdfConnection.load(dateSet);
        return null;
    }
}
