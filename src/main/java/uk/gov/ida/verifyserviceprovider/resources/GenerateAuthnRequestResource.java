package uk.gov.ida.verifyserviceprovider.resources;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.LoggerFactory;
import uk.gov.ida.saml.serializers.XmlObjectToBase64EncodedStringTransformer;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;
import uk.gov.ida.verifyserviceprovider.logging.AuthnRequestAttributesLogger;
import uk.gov.ida.verifyserviceprovider.services.EntityIdService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/generate-request")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GenerateAuthnRequestResource {

    private final URI ssoLocation;
    private final AuthnRequestFactory authnRequestFactory;
    private final EntityIdService entityIdService;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GenerateAuthnRequestResource.class);

    public GenerateAuthnRequestResource(AuthnRequestFactory authnRequestFactory, URI ssoLocation, EntityIdService entityIdService) {
        this.authnRequestFactory = authnRequestFactory;
        this.ssoLocation = ssoLocation;
        this.entityIdService = entityIdService;
    }

    @POST
    public Response generateAuthnRequest(@NotNull @Valid RequestGenerationBody requestGenerationBody) {
        String entityId = entityIdService.getEntityId(requestGenerationBody);
        AuthnRequest authnRequest = this.authnRequestFactory.build(requestGenerationBody.getLevelOfAssurance(), entityId);
        XmlObjectToBase64EncodedStringTransformer xmlToBase64Transformer = new XmlObjectToBase64EncodedStringTransformer();
        String samlRequest = xmlToBase64Transformer.apply(authnRequest);

        RequestResponseBody requestResponseBody = new RequestResponseBody(samlRequest, authnRequest.getID(), ssoLocation);

        LOG.info(String.format("AuthnRequest generated for entityId: %s with requestId: %s", entityId, requestResponseBody.getRequestId()));
        AuthnRequestAttributesLogger.logAuthnRequestAttributes(authnRequest);
        LOG.debug(String.format("AuthnRequest generated for entityId: %s with saml: %s", entityId, requestResponseBody.getSamlRequest()));

        return Response.ok(requestResponseBody).build();
    }
}
