
package ec.edu.epn.petclinic.owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad Pet.
 */
@DisplayName("Pet Entity Tests")
class PetTest {

    private Validator validator;
    private Pet pet;
    private PetType petType;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Arrange: Pet válido por defecto
        petType = new PetType();
        petType.setName("Dog");
        
        pet = new Pet();
        pet.setName("Fluffy");
        pet.setBirthDate(LocalDate.of(2020, 1, 15));
        pet.setType(petType);
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid pet should pass validation")
        void validPet_shouldPassValidation() {
            // Act
            Set<ConstraintViolation<Pet>> violations = validator.validate(pet);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Pet without name should still be valid (no @NotBlank on Pet.name)")
        void petWithoutName_shouldBeValid() {
            // Arrange
            pet.setName(null);

            // Act
            Set<ConstraintViolation<Pet>> violations = validator.validate(pet);

            // Assert - Pet entity doesn't have @NotBlank on name (inherits from NamedEntity)
            // This is a design decision in the original code
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Birth Date Tests")
    class BirthDateTests {

        @Test
        @DisplayName("Pet can have birth date in the past")
        void pastBirthDate_shouldBeValid() {
            // Arrange
            pet.setBirthDate(LocalDate.of(2015, 6, 10));

            // Act
            Set<ConstraintViolation<Pet>> violations = validator.validate(pet);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Pet can have today as birth date")
        void todayBirthDate_shouldBeValid() {
            // Arrange
            pet.setBirthDate(LocalDate.now());

            // Act
            Set<ConstraintViolation<Pet>> violations = validator.validate(pet);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("getBirthDate should return set value")
        void getBirthDate_shouldReturnSetValue() {
            // Arrange
            LocalDate expected = LocalDate.of(2021, 3, 25);
            pet.setBirthDate(expected);

            // Act
            LocalDate actual = pet.getBirthDate();

            // Assert
            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Pet Type Tests")
    class PetTypeTests {

        @Test
        @DisplayName("getType should return set pet type")
        void getType_shouldReturnSetType() {
            // Arrange
            PetType cat = new PetType();
            cat.setName("Cat");
            pet.setType(cat);

            // Act
            PetType result = pet.getType();

            // Assert
            assertThat(result)
                .isNotNull()
                .isEqualTo(cat);
            assertThat(result.getName()).isEqualTo("Cat");
        }

        @Test
        @DisplayName("Pet can exist without type")
        void petWithoutType_shouldBeAllowed() {
            // Arrange
            pet.setType(null);

            // Act
            PetType result = pet.getType();

            // Assert
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Visit Management Tests")
    class VisitManagementTests {

        @Test
        @DisplayName("New pet should have empty visits collection")
        void newPet_shouldHaveEmptyVisits() {
            // Arrange
            Pet newPet = new Pet();

            // Assert
            assertThat(newPet.getVisits()).isEmpty();
        }

        @Test
        @DisplayName("addVisit should add visit to pet")
        void addVisit_shouldAddVisitToPet() {
            // Arrange
            Visit visit1 = new Visit();
            visit1.setDescription("Checkup");
            visit1.setDate(LocalDate.of(2023, 5, 10));

            Visit visit2 = new Visit();
            visit2.setDescription("Vaccination");
            visit2.setDate(LocalDate.of(2023, 6, 15));

            // Act
            pet.addVisit(visit1);
            pet.addVisit(visit2);

            // Assert
            assertThat(pet.getVisits())
                .hasSize(2)
                .containsExactly(visit1, visit2);
        }

        @Test
        @DisplayName("Visits should maintain insertion order")
        void visits_shouldMaintainInsertionOrder() {
            // Arrange
            Visit visit1 = new Visit();
            visit1.setDescription("First");
            visit1.setDate(LocalDate.now());

            Visit visit2 = new Visit();
            visit2.setDescription("Second");
            visit2.setDate(LocalDate.now().plusDays(1));

            Visit visit3 = new Visit();
            visit3.setDescription("Third");
            visit3.setDate(LocalDate.now().plusDays(2));

            // Act
            pet.addVisit(visit1);
            pet.addVisit(visit2);
            pet.addVisit(visit3);

            // Assert
            assertThat(pet.getVisits())
                .hasSize(3)
                .containsExactly(visit1, visit2, visit3);
        }

        @Test
        @DisplayName("getVisits should return modifiable collection")
        void getVisits_shouldReturnModifiableCollection() {
            // Arrange
            Visit visit = new Visit();
            visit.setDescription("Test");

            // Act & Assert
            assertThatCode(() -> pet.getVisits().add(visit))
                .doesNotThrowAnyException();
            
            assertThat(pet.getVisits()).contains(visit);
        }
    }

    @Nested
    @DisplayName("Entity Lifecycle Tests")
    class EntityLifecycleTests {

        @Test
        @DisplayName("New pet should have null ID")
        void newPet_shouldHaveNullId() {
            // Arrange
            Pet newPet = new Pet();

            // Assert
            assertThat(newPet.getId()).isNull();
        }

        @Test
        @DisplayName("isNew should return true for pet without ID")
        void isNew_withoutId_shouldReturnTrue() {
            // Arrange
            Pet newPet = new Pet();

            // Assert
            assertThat(newPet.isNew()).isTrue();
        }

        @Test
        @DisplayName("isNew should return false for pet with ID")
        void isNew_withId_shouldReturnFalse() {
            // Arrange
            pet.setId(123);

            // Assert
            assertThat(pet.isNew()).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Pet can have very long name")
        void veryLongName_shouldBeAccepted() {
            // Arrange
            String longName = "A".repeat(500);
            pet.setName(longName);

            // Act
            String result = pet.getName();

            // Assert
            assertThat(result).hasSize(500);
        }

        @Test
        @DisplayName("Multiple pets can share the same type")
        void multiplePets_sameType_shouldBeAllowed() {
            // Arrange
            Pet dog1 = new Pet();
            dog1.setName("Max");
            dog1.setType(petType);

            Pet dog2 = new Pet();
            dog2.setName("Buddy");
            dog2.setType(petType);

            // Assert
            assertThat(dog1.getType()).isSameAs(dog2.getType());
        }

        @Test
        @DisplayName("Adding duplicate visit should be allowed (Set behavior)")
        void addDuplicateVisit_shouldBeAllowed() {
            // Arrange
            Visit visit = new Visit();
            visit.setDescription("Same visit");
            visit.setId(1);

            // Act
            pet.addVisit(visit);
            pet.addVisit(visit);

            // Assert - Since visits is a Set, duplicates are not added
            assertThat(pet.getVisits()).hasSize(1);
        }
    }
}
