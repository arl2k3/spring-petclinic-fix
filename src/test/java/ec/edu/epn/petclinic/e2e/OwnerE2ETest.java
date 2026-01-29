
package ec.edu.epn.petclinic.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Owner E2E Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OwnerE2ETest {

    @LocalServerPort
    private int port;

    private static Integer createdOwnerId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Create new owner successfully")
    void createOwner_validData_shouldSucceed() {
        // Arrange
        String formData = "firstName=Integration&lastName=TestOwner&" +
                "address=123 Test Street&city=TestCity&telephone=1234567890";

        // Act & Assert
        String location = given()
                .contentType(ContentType.URLENC)
                .body(formData)
                .when()
                .post("/owners/new")
                .then()
                .statusCode(302) // Redirect
                .header("Location", matchesPattern(".*/owners/\\d+.*")) // Allow jsessionid in pattern
                .extract()
                .header("Location");

        // Remove jsessionid and extract owner ID from redirect URL
        String cleanLocation = location.replaceAll(";jsessionid=[^/]*", "");
        createdOwnerId = Integer.parseInt(cleanLocation.replaceAll(".*/owners/(\\d+).*", "$1"));

        System.out.println("Created owner with ID: " + createdOwnerId);
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Retrieve created owner details")
    void getOwnerDetails_existingOwner_shouldReturnDetails() {
        // Ensure we have an owner ID
        Assumptions.assumeTrue(createdOwnerId != null, "Owner must be created first");

        // Act & Assert
        given()
                .accept(ContentType.HTML)
                .when()
                .get("/owners/" + createdOwnerId)
                .then()
                .statusCode(200)
                .body(containsString("Integration"))
                .body(containsString("TestOwner"))
                .body(containsString("123 Test Street"))
                .body(containsString("TestCity"))
                .body(containsString("1234567890"));
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Update existing owner")
    void updateOwner_validData_shouldSucceed() {
        Assumptions.assumeTrue(createdOwnerId != null);

        // Arrange
        String updatedFormData = "id=" + createdOwnerId +
                "&firstName=UpdatedFirst&lastName=UpdatedLast&" +
                "address=999 Updated Ave&city=NewCity&telephone=9876543210";

        // Act & Assert
        given()
                .contentType(ContentType.URLENC)
                .body(updatedFormData)
                .when()
                .post("/owners/" + createdOwnerId + "/edit")
                .then()
                .statusCode(302)
                .header("Location", endsWith("/owners/" + createdOwnerId));

        // Verify update
        given()
                .accept(ContentType.HTML)
                .when()
                .get("/owners/" + createdOwnerId)
                .then()
                .statusCode(200)
                .body(containsString("UpdatedFirst"))
                .body(containsString("UpdatedLast"))
                .body(containsString("999 Updated Ave"));
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Search for owner by last name")
    void searchOwner_byLastName_shouldFindOwner() {
        Assumptions.assumeTrue(createdOwnerId != null);

        // Act & Assert - search should redirect to single owner
        given()
                .queryParam("lastName", "UpdatedLast")
                .queryParam("page", 1)
                .when()
                .get("/owners")
                .then()
                .statusCode(anyOf(is(200), is(302))); // May redirect if single result
    }

    @Test
    @DisplayName("E2E: Create owner with invalid data should show errors")
    void createOwner_invalidTelephone_shouldShowErrors() {
        // Arrange - telephone with invalid format
        String invalidFormData = "firstName=Invalid&lastName=Owner&" +
                "address=123 St&city=City&telephone=123"; // Only 3 digits

        // Act & Assert
        given()
                .contentType(ContentType.URLENC)
                .body(invalidFormData)
                .when()
                .post("/owners/new")
                .then()
                .statusCode(200) // Stays on form page
                .body(containsString("add-owner-form")); // Check for form ID instead of view name
    }

    @Test
    @DisplayName("E2E: Create owner with missing required fields should fail")
    void createOwner_missingFields_shouldShowErrors() {
        // Arrange - only firstName, missing other required fields
        String incompleteFormData = "firstName=Incomplete";

        // Act & Assert
        given()
                .contentType(ContentType.URLENC)
                .body(incompleteFormData)
                .when()
                .post("/owners/new")
                .then()
                .statusCode(200)
                .body(containsString("add-owner-form")); // Check for form ID instead of view name
    }

    @Test
    @DisplayName("E2E: Get non-existent owner should return error")
    void getOwner_nonExistentId_shouldReturnError() {
        // Act & Assert
        given()
                .accept(ContentType.HTML)
                .when()
                .get("/owners/99999")
                .then()
                .statusCode(404); // EntityNotFoundException returns 404
    }

    @Test
    @DisplayName("E2E: Search with no results should show find form")
    void searchOwner_noResults_shouldShowFindForm() {
        // Act & Assert
        given()
                .queryParam("lastName", "NonExistentLastName12345")
                .queryParam("page", 1)
                .when()
                .get("/owners")
                .then()
                .statusCode(200)
                .body(containsString("not found")); // Error message for no results
    }

    @Test
    @DisplayName("E2E: Search without lastName should return all owners")
    void searchOwner_noLastName_shouldReturnAll() {
        // Act & Assert
        given()
                .queryParam("page", 1)
                .when()
                .get("/owners")
                .then()
                .statusCode(200)
                .body(containsString("Owners")); // Page contains Owners text
    }

    @Test
    @DisplayName("E2E: Update owner with ID mismatch should show error")
    void updateOwner_idMismatch_shouldShowError() {
        Assumptions.assumeTrue(createdOwnerId != null);

        // Arrange - ID in form doesn't match URL
        String mismatchFormData = "id=99999&firstName=Test&lastName=Test&" +
                "address=123 St&city=City&telephone=1234567890";

        // Act & Assert
        given()
                .contentType(ContentType.URLENC)
                .body(mismatchFormData)
                .when()
                .post("/owners/" + createdOwnerId + "/edit")
                .then()
                .statusCode(302);
    }

    @Test
    @DisplayName("E2E: Display find owners form")
    void displayFindOwnersForm_shouldSucceed() {
        // Act & Assert
        given()
                .accept(ContentType.HTML)
                .when()
                .get("/owners/find")
                .then()
                .statusCode(200)
                .body(containsString("Find Owners")); // Check for visible text
    }

    @Test
    @DisplayName("E2E: Display create owner form")
    void displayCreateOwnerForm_shouldSucceed() {
        // Act & Assert
        given()
                .accept(ContentType.HTML)
                .when()
                .get("/owners/new")
                .then()
                .statusCode(200)
                .body(containsString("add-owner-form")); // Form ID in HTML
    }
}
