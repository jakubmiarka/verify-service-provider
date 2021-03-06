package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.dropwizard.servlets.assets.ResourceNotFoundException;
import uk.gov.ida.saml.metadata.KeyStoreLoader;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.Arrays;

import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.COMPLIANCE_HUBCONNECTOR_ENTITY_ID;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.COMPLIANCE_METADATA;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.COMPLIANCE_METADATASOURCE_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.COMPLIANCE_SSO;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.COMPLIANCE_TRUSTANCHOR_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.DEFAULT_TRUST_STORE_PASSWORD;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.INTEGRATION_HUBCONNECTOR_ENTITY_ID;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.INTEGRATION_METADATA;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.INTEGRATION_METADATASOURCE_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.INTEGRATION_SSO;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.INTEGRATION_TRUSTANCHOR_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_HUBCONNECTOR_ENTITY_ID;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_HUB_TRUSTSTORE;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_IDP_TRUSTSTORE;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_METADATA;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_METADATASOURCE_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_METADATA_TRUSTSTORE;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_SSO;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_TRUSTANCHOR_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_HUB_TRUSTSTORE;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_IDP_TRUSTSTORE;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_METADATA_TRUSTSTORE;

public enum HubEnvironment {
    PRODUCTION(
            URI.create(PRODUCTION_SSO),
            URI.create(PRODUCTION_METADATA),
            URI.create(PRODUCTION_METADATASOURCE_URI),
            URI.create(PRODUCTION_TRUSTANCHOR_URI),
            PRODUCTION_HUBCONNECTOR_ENTITY_ID,
            PRODUCTION_METADATA_TRUSTSTORE, PRODUCTION_HUB_TRUSTSTORE, PRODUCTION_IDP_TRUSTSTORE),
    INTEGRATION(
            URI.create(INTEGRATION_SSO),
            URI.create(INTEGRATION_METADATA),
            URI.create(INTEGRATION_METADATASOURCE_URI),
            URI.create(INTEGRATION_TRUSTANCHOR_URI),
            INTEGRATION_HUBCONNECTOR_ENTITY_ID,
            TEST_METADATA_TRUSTSTORE, TEST_HUB_TRUSTSTORE, TEST_IDP_TRUSTSTORE),
    COMPLIANCE_TOOL(
            URI.create(COMPLIANCE_SSO),
            URI.create(COMPLIANCE_METADATA),
            URI.create(COMPLIANCE_METADATASOURCE_URI),
            URI.create(COMPLIANCE_TRUSTANCHOR_URI),
            COMPLIANCE_HUBCONNECTOR_ENTITY_ID,
            TEST_METADATA_TRUSTSTORE, TEST_HUB_TRUSTSTORE, TEST_IDP_TRUSTSTORE);


    private URI ssoLocation;
    private URI metadataUri;

    private URI eidasMetaDataSourceUri;
    private URI eidasMetadataTrustAnchorUri;
    private String eidasHubConnectorEntityId;
    private String metadataTrustStore;
    private String hubTrustStore;
    private String idpTrustStore;

    @JsonCreator
    public static HubEnvironment fromString(String name) {
        return Arrays.stream(values())
            .filter(x -> name.equals(x.name()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Unrecognised Hub Environment: " + name + ". \n" +
                "Valid values are: PRODUCTION, INTEGRATION, COMPLIANCE_TOOL"
            ));
    }

    HubEnvironment(URI ssoLocation, URI metadataUri, URI eidasMetadataSourceUri, URI eidasMetadataTrustAnchorUri, String eidasHubConnectorEntityId, String metadataTrustStore, String hubTrustStore, String idpTrustStore) {
        this.ssoLocation = ssoLocation;
        this.metadataUri = metadataUri;
        this.eidasMetaDataSourceUri = eidasMetadataSourceUri;
        this.eidasMetadataTrustAnchorUri = eidasMetadataTrustAnchorUri;
        this.eidasHubConnectorEntityId = eidasHubConnectorEntityId;
        this.metadataTrustStore = metadataTrustStore;
        this.hubTrustStore = hubTrustStore;
        this.idpTrustStore = idpTrustStore;
    }

    public URI getSsoLocation() {
        return this.ssoLocation;
    }

    public URI getMetadataUri() {
        return this.metadataUri;
    }

    public URI getEidasMetadataSourceUri() {
        return this.eidasMetaDataSourceUri;
    }

    public URI getEidasMetadataTrustAnchorUri() {
        return this.eidasMetadataTrustAnchorUri;
    }

    public String getEidasHubConnectorEntityId(){
        return this.eidasHubConnectorEntityId;
    }

    public KeyStore getMetadataTrustStore() {
        return loadTrustStore(metadataTrustStore);
    }

    public KeyStore getHubTrustStore() {
        return loadTrustStore(hubTrustStore);
    }

    public KeyStore getIdpTrustStore() {
        return loadTrustStore(idpTrustStore);
    }

    private KeyStore loadTrustStore(String trustStoreName) {
        InputStream trustStoreStream = getClass().getClassLoader().getResourceAsStream(trustStoreName);
        if (trustStoreStream == null) {
            throw new ResourceNotFoundException(new FileNotFoundException("Could not load resource from path " + trustStoreName));
        }
        return new KeyStoreLoader().load(trustStoreStream, DEFAULT_TRUST_STORE_PASSWORD);
    }
}
