package org.realitix.dfilesearch.filesearch.resources;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.webservice.beans.FileResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/file")
@Produces(MediaType.APPLICATION_JSON)
public class FileSharingResource {

    private static final Logger logger = LogManager.getLogger(FileSharingResource.class);

    @GET
    @Path("{fileName}")
    public Response getFile(@PathParam("fileName") String fileName) {
        return Response.status(200).entity(synthesizeFile(fileName)).build();
    }

    private FileResponse synthesizeFile(String fileName){
        logger.info("Synthesizing the file");
        String randomString = fileName + RandomStringUtils.randomAlphabetic(20).toUpperCase();
        int size = (int) ((Math.random() * ((10 - 2) + 1)) + 2);    // change this to a more random algorithm
        FileResponse fileResponse = new FileResponse();
        fileResponse.setFileSize(size);
        fileResponse.setHash(DigestUtils.sha1Hex(randomString));
        logger.info("File synthesizing completed.");
        return fileResponse;
    }

}
