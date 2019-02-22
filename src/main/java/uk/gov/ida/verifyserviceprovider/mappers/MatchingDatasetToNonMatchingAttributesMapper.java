package uk.gov.ida.verifyserviceprovider.mappers;

import org.joda.time.DateTime;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.domain.TransliterableMdsValue;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAddress;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingVerifiableAttribute;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchingDatasetToNonMatchingAttributesMapper {
    static <T> Comparator<NonMatchingVerifiableAttribute<T>> attributeComparator() {
        return Comparator.<NonMatchingVerifiableAttribute<T>, LocalDate>comparing(NonMatchingVerifiableAttribute::getTo, Comparator.nullsFirst(Comparator.reverseOrder()))
                .thenComparing(NonMatchingVerifiableAttribute::isVerified, Comparator.reverseOrder())
                .thenComparing(NonMatchingVerifiableAttribute::getFrom, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    public NonMatchingAttributes mapToNonMatchingAttributes(MatchingDataset matchingDataset) {
        return new NonMatchingAttributes(
                convertTransliterableNameAttributes(matchingDataset.getFirstNames()),
                convertNameAttributes(matchingDataset.getMiddleNames()),
                convertTransliterableNameAttributes(matchingDataset.getSurnames()),
                convertDateOfBirths(matchingDataset.getDateOfBirths()),
                convertToGenderAttribute(matchingDataset),
                mapAddresses(matchingDataset.getAddresses())
        );
    }

    private NonMatchingVerifiableAttribute<Gender> convertToGenderAttribute(MatchingDataset matchingDataset) {
        return matchingDataset.getGender()
                    .map(this::mapToNonMatchingVerifiableAttribute)
                    .orElse(null);
    }

    private List<NonMatchingVerifiableAttribute<String>> convertNameAttributes(List<SimpleMdsValue<String>> values) {
        return values.stream()
                .map(this::mapToNonMatchingVerifiableAttribute)
                .sorted(attributeComparator())
                .collect(Collectors.toList());
    }

    private List<NonMatchingVerifiableAttribute<LocalDate>> convertDateOfBirths(List<SimpleMdsValue<org.joda.time.LocalDate>> values) {
        return values.stream()
                .map(this::convertWrappedJodaLocalDateToJavaLocalDate)
                .map(this::mapToNonMatchingVerifiableAttribute)
                .sorted(attributeComparator())
                .collect(Collectors.toList());
    }

    private List<NonMatchingVerifiableAttribute<String>> convertTransliterableNameAttributes(List<TransliterableMdsValue> values) {
        return values.stream()
                .map(this::mapToNonMatchingVerifiableAttribute)
                .sorted(attributeComparator())
                .collect(Collectors.toList());
    }

    private <T> NonMatchingVerifiableAttribute<T> mapToNonMatchingVerifiableAttribute(SimpleMdsValue<T> simpleMdsValue) {
        LocalDate from = convertToLocalDate(simpleMdsValue.getFrom());
        LocalDate to = convertToLocalDate(simpleMdsValue.getTo());

        return new NonMatchingVerifiableAttribute<>(
            simpleMdsValue.getValue(),
            simpleMdsValue.isVerified(),
            from,
            to
        );
    }

    private List<NonMatchingVerifiableAttribute<NonMatchingAddress>> mapAddresses(List<Address> addresses) {
        return addresses.stream().map(this::mapAddress).sorted(attributeComparator()).collect(Collectors.toList());
    }

    private NonMatchingVerifiableAttribute<NonMatchingAddress> mapAddress(Address input) {
        NonMatchingAddress transformedAddress = new NonMatchingAddress(
            input.getLines(),
            input.getPostCode().orElse(null),
            input.getInternationalPostCode().orElse(null),
            input.getUPRN().orElse(null)
        );

        LocalDate from = convertToLocalDate(input.getFrom());

        LocalDate to = input.getTo()
                .map(this::convertToLocalDate)
                .orElse(null);

        return new NonMatchingVerifiableAttribute<>(
                transformedAddress,
                input.isVerified(),
                from,
                to
        );
    }

    private LocalDate convertToLocalDate(DateTime dateTime) {
        return Optional.ofNullable(dateTime)
                .map(jodaDateTime -> LocalDate.of(
                        jodaDateTime.getYear(),
                        jodaDateTime.getMonthOfYear(),
                        jodaDateTime.getDayOfMonth()
                ))
                .orElse(null);
    }

    private LocalDate convertJodaLocalDateToJavaLocalDate(org.joda.time.LocalDate jodaDate) {
        return LocalDate.of(jodaDate.getYear(), jodaDate.getMonthOfYear(), jodaDate.getDayOfMonth());
    }

    private SimpleMdsValue<LocalDate> convertWrappedJodaLocalDateToJavaLocalDate(SimpleMdsValue<org.joda.time.LocalDate> wrappedJodaDate) {
        LocalDate javaLocalDate = convertJodaLocalDateToJavaLocalDate(wrappedJodaDate.getValue());
        return new SimpleMdsValue<>(javaLocalDate, wrappedJodaDate.getFrom(), wrappedJodaDate.getTo(), wrappedJodaDate.isVerified());
    }
}
