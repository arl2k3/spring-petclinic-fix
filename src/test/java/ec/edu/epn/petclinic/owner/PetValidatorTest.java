
package ec.edu.epn.petclinic.owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas unitarias para PetValidator.
 */
@DisplayName("PetValidator Tests")
class PetValidatorTest {

    private PetValidator validator;
    private Pet pet;
    private Errors errors;

    @BeforeEach
    void setUp() {
        validator = new PetValidator();
        pet = new Pet();
        pet.setName("Fluffy");

        PetType type = new PetType();
        type.setName("Dog");
        pet.setType(type);
        pet.setBirthDate(LocalDate.of(2020, 1, 1));

        errors = new BeanPropertyBindingResult(pet, "pet");
    }

    @Test
    @DisplayName("Validator should support Pet class")
    void supports_shouldSupportPetClass() {
        // Act
        boolean result = validator.supports(Pet.class);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Validator should not support other classes")
    void supports_shouldNotSupportOtherClasses() {
        // Act
        boolean result = validator.supports(String.class);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Valid pet should pass validation")
    void validPet_shouldPassValidation() {
        // Act
        validator.validate(pet, errors);

        // Assert
        assertThat(errors.hasErrors()).isFalse();
    }

    @ParameterizedTest(name = "Name [{0}] should pass validation")
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "\t\n  \r", "Max Von Fluffington III" })
    @DisplayName("Pet with various name values should pass validation (name not required by PetValidator)")
    void petWithVariousNames_shouldPassValidation(String nameValue) {
        // Arrange
        pet.setName(nameValue);

        // Act
        validator.validate(pet, errors);

        // Assert - Name is not required by PetValidator
        assertThat(errors.hasFieldErrors("name")).isFalse();
    }

    @Test
    @DisplayName("Pet without type should fail validation")
    void petWithoutType_shouldFailValidation() {
        // Arrange
        pet.setType(null);

        // Act
        validator.validate(pet, errors);

        // Assert
        assertThat(errors.hasFieldErrors("type")).isTrue();
        assertThat(errors.getFieldError("type"))
                .isNotNull()
                .extracting("code")
                .isEqualTo("required");
    }

    @Test
    @DisplayName("Pet without birth date should fail validation")
    void petWithoutBirthDate_shouldFailValidation() {
        // Arrange
        pet.setBirthDate(null);

        // Act
        validator.validate(pet, errors);

        // Assert
        assertThat(errors.hasFieldErrors("birthDate")).isTrue();
        assertThat(errors.getFieldError("birthDate"))
                .isNotNull()
                .extracting("code")
                .isEqualTo("required");
    }

    @Test
    @DisplayName("Multiple validation errors should be reported")
    void multipleErrors_shouldAllBeReported() {
        // Arrange
        pet.setName(null);
        pet.setType(null);
        pet.setBirthDate(null);

        // Act
        validator.validate(pet, errors);

        // Assert - Only type and birthDate are validated, not name
        assertThat(errors.getErrorCount()).isEqualTo(2);
        assertThat(errors.hasFieldErrors("type")).isTrue();
        assertThat(errors.hasFieldErrors("birthDate")).isTrue();
    }

    @Test
    @DisplayName("Pet with valid future birth date should pass validator")
    void futureBirthDate_shouldPassValidator() {
        // Note: PetValidator doesn't validate future dates
        // That validation is done in PetController

        // Arrange
        pet.setBirthDate(LocalDate.now().plusDays(10));

        // Act
        validator.validate(pet, errors);

        // Assert - PetValidator only checks for null
        assertThat(errors.hasFieldErrors("birthDate")).isFalse();
    }
}
