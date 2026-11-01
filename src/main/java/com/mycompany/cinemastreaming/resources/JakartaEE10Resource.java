package com.mycompany.cinemastreaming.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author
 */
/**
 * Owner: Toàn nhóm.
 * Role: Jakarta EE application configuration/resource.
 * Source: src/main/java/com/mycompany/cinemastreaming/resources/JakartaEE10Resource.java.
 */
@Path("jakartaee10")
public class JakartaEE10Resource {

    /** Owner: Toàn nhóm. Purpose: return the resource health response. */
    @GET
    public Response ping() {
        return Response.ok("ping Jakarta EE").build();
    }
}
