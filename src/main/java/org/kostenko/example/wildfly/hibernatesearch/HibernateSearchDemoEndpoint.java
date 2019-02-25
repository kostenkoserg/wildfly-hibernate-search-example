package org.kostenko.example.wildfly.hibernatesearch;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *
 * @author kostenko
 */
@Path("/")
@Stateless
public class HibernateSearchDemoEndpoint {

    @GET
    @Path("/init")
    public Response init() {
        return Response.ok().entity("OK").build();
    }

    @GET
    @Path("/search")
    public Response search(@QueryParam("q") String query) {
        return Response.ok().entity("Bye").build();
    }

}
