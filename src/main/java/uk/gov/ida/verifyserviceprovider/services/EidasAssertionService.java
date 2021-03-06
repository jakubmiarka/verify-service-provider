package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.SignatureValidatorFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario.IDENTITY_VERIFIED;

public class EidasAssertionService extends AssertionServiceV2 {

    private final boolean isEnabled;
    private final InstantValidator instantValidator;
    private final ConditionsValidator conditionsValidator;
    private final LevelOfAssuranceValidator levelOfAssuranceValidator;
    private final Optional<EidasMetadataResolverRepository> metadataResolverRepository;
    private final SignatureValidatorFactory signatureValidatorFactory;
    private final Optional<String> entityId;
    private UserIdHashFactory userIdHashFactory;
    private final AuthnContextFactory authnContextFactory = new AuthnContextFactory();


    public EidasAssertionService(
            boolean isEnabled,
            SubjectValidator subjectValidator,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            MatchingDatasetToNonMatchingAttributesMapper mdsMapper,
            InstantValidator instantValidator,
            ConditionsValidator conditionsValidator,
            LevelOfAssuranceValidator levelOfAssuranceValidator,
            Optional<EidasMetadataResolverRepository> metadataResolverRepository,
            SignatureValidatorFactory signatureValidatorFactory,
            Optional<String> entityId,
            UserIdHashFactory userIdHashFactory) {
        super(subjectValidator, matchingDatasetUnmarshaller, mdsMapper);
        this.isEnabled = isEnabled;
        this.instantValidator = instantValidator;
        this.conditionsValidator = conditionsValidator;
        this.levelOfAssuranceValidator = levelOfAssuranceValidator;
        this.metadataResolverRepository = metadataResolverRepository;
        this.signatureValidatorFactory = signatureValidatorFactory;
        this.entityId = entityId;
        this.userIdHashFactory = userIdHashFactory;
    }


    @Override
    public TranslatedNonMatchingResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        if (assertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one country assertion is expected.");
        }

        Assertion countryAssertion = assertions.get(0);

        validateCountryAssertion(countryAssertion, expectedInResponseTo);

        LevelOfAssurance levelOfAssurance = extractLevelOfAssuranceFrom(countryAssertion);
        levelOfAssuranceValidator.validate(levelOfAssurance, expectedLevelOfAssurance);

        String nameID = getNameIdFrom(countryAssertion);
        String issuerID = countryAssertion.getIssuer().getValue();
        String levelOfAssuranceUri =  extractLevelOfAssuranceUriFrom(countryAssertion);
        Optional<AuthnContext> authnContext = getAuthnContext(levelOfAssuranceUri);
        
        String hashId = userIdHashFactory.hashId(issuerID, nameID, authnContext);

        NonMatchingAttributes attributes = translateAttributes(countryAssertion);

        return new TranslatedNonMatchingResponseBody(IDENTITY_VERIFIED, hashId, levelOfAssurance, attributes);
    }

    private void validateCountryAssertion(Assertion assertion, String expectedInResponseTo) {
        signatureValidatorFactory.getSignatureValidator(metadataResolverRepository.get().getSignatureTrustEngine(assertion.getIssuer().getValue()))
                .orElseThrow(() -> new SamlResponseValidationException("Unable to find metadata resolver for entity Id " + assertion.getIssuer().getValue()))
                .validate(singletonList(assertion), IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        instantValidator.validate(assertion.getIssueInstant(), "Country Assertion IssueInstant");
        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        conditionsValidator.validate(assertion.getConditions(), entityId.get());
    }

    public LevelOfAssurance extractLevelOfAssuranceFrom(Assertion countryAssertion) {
        String levelOfAssuranceString = extractLevelOfAssuranceUriFrom(countryAssertion);

        try {
            return LevelOfAssurance.fromSamlValue(new AuthnContextFactory().mapFromEidasToLoA(levelOfAssuranceString).getUri());
        } catch (Exception ex) {
            throw new SamlResponseValidationException(String.format("Level of assurance '%s' is not supported.", levelOfAssuranceString));
        }
    }

    private Optional<AuthnContext> getAuthnContext(String uri) {
        AuthnContext authnContext = authnContextFactory.mapFromEidasToLoA(uri);

        return Optional.of(authnContext);
    }

    public Boolean isCountryAssertion(Assertion assertion) {
        return isEnabled && metadataResolverRepository.get().getResolverEntityIds().contains(assertion.getIssuer().getValue());
    }

}
