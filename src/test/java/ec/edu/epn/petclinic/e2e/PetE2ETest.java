
package ec.edu.epn.petclinic.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Pet E2E Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PetE2ETest {

    @LocalServerPort
    private int port;

    private static Integer testOwnerId;
    private static Integer testPetId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Setup - Create owner for pet tests")
    void setupOwner_shouldSucceed() {
        // Create an owner first
        String formData = "firstName=PetOwner&lastName=TestUser&" +
                         "address=456 Pet Street&city=PetCity&telephone=5555555555";

        String location = given()
            .contentType(ContentType.URLENC)
            .body(formData)
        .when()
            .post("/owners/new")
        .then()
            .statusCode(302)
            .extract()
            .header("Location");

        testOwnerId = Integer.parseInt(location.replaceAll(".*/owners/(\\d+)", "$1"));
        System.out.println("Created test owner with ID: " + testOwnerId);
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Display add pet form")
    void displayAddPetForm_shouldSucceed() {
        Assumptions.assumeTrue(testOwnerId != null);

        // Act & Assert
        given()
            .accept(ContentType.HTML)
        .when()
            .get("/owners/" + testOwnerId + "/pets/new")
        .then()
            .statusCode(200)
            .body(containsString("createOrUpdatePetForm"));
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Add new pet to owner successfully")
    void addPet_validData_shouldSucceed() {
        Assumptions.assumeTrue(testOwnerId != null);

        // Arrange
        String petFormData = "name=Fluffy&birthDate=2020-01-15&type=cat";

        // Act & Assert
        String location = given()
            .contentType(ContentType.URLENC)
            .body(petFormData)
        .when()
            .post("/owners/" + testOwnerId + "/pets/new")
        .then()
            .statusCode(302)
            .header("Location", endsWith("/owners/" + testOwnerId))
            .extract()
            .header("Location");

        System.out.println("Added pet to owner, redirected to: " + location);

        // Verify pet appears in owner details
        given()
            .accept(ContentType.HTML)
        .when()
            .get("/owners/" + testOwnerId)
        .then()
            .statusCode(200)
            .body(containsString("Fluffy"));
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Add pet with duplicate name should fail")
    void addPet_duplicateName_shouldFail() {
        Assumptions.assumeTrue(testOwnerId != null);

        // Try to add another pet with the same name
        String duplicatePetData = "name=Fluffy&birthDate=2021-06-10&type=dog";

        given()
            .contentType(ContentType.URLENC)
            .body(duplicatePetData)
        .when()
            .post("/owners/" + testOwnerId + "/pets/new")
        .then()
            .statusCode(200)  // Stays on form
            .body(containsString("already exists"));
    }

    @Test
    @DisplayName("E2E: Add pet with future birth date should fail")
    void addPet_futureBirthDate_shouldFail() {
        Assumptions.assumeTrue(testOwnerId != null);

        // Arrange - birth date in the future
        String futurePetData = "name=FuturePet&birthDate=2030-12-31&type=bird";

        // Act & Assert
        given()
            .contentType(ContentType.URLENC)
            .body(futurePetData)
        .when()
            .post("/owners/" + testOwnerId + "/pets/new")
        .then()
            .statusCode(200)  // Stays on form due to validation error
            .body(containsString("createOrUpdatePetForm"));
    }

    @Test
    @DisplayName("E2E: Add pet with missing required fields should fail")
    void addPet_missingFields_shouldFail() {
        Assumptions.assumeTrue(testOwnerId != null);

        // Arrange - missing birthDate and type
        String incompleteData = "name=IncompletePet";

        // Act & Assert
        given()
            .contentType(ContentType.URLENC)
            .body(incompleteData)
        .when()
            .post("/owners/" + testOwnerId + "/pets/new")
        .then()
            .statusCode(200)
            .body(containsString("createOrUpdatePetForm"));
    }

    @Test
    @DisplayName("E2E: Add pet to non-existent owner should fail")
    void addPet_nonExistentOwner_shouldFail() {
        // Arrange
        String petData = "name=OrphanPet&birthDate=2020-05-05&type=hamster";

        // Act & Assert
        given()
            .contentType(ContentType.URLENC)
            .body(petData)
        .when()
            .post("/owners/99999/pets/new")
        .then()
            .statusCode(500);  // IllegalArgumentException
    }

    @Test
    @DisplayName("E2E: Display add pet form for non-existent owner should fail")
    void displayAddPetForm_nonExistentOwner_shouldFail() {
        // Act & Assert
        given()
            .accept(ContentType.HTML)
        .when()
            .get("/owners/99999/pets/new")
        .then()
            .statusCode(500);
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Complete flow - Create owner, add pet, add visit")
    void completeFlow_ownerPetVisit_shouldSucceed() {
        // Step 1: Create owner
        String ownerData = "firstName=Complete&lastName=FlowTest&" +
                          "address=789 Flow St&city=FlowCity&telephone=7777777777";

        String ownerLocation = given()
            .contentType(ContentType.URLENC)
            .body(ownerData)
        .when()
            .post("/owners/new")
        .then()
            .statusCode(302)
            .extract()
            .header("Location");

        Integer flowOwnerId = Integer.parseInt(ownerLocation.replaceAll(".*/owners/(\\d+)", "$1"));

        // Step 2: Add pet to owner
        String petData = "name=CompleteFlowPet&birthDate=2019-03-20&type=lizard";

        given()
            .contentType(ContentType.URLENC)
            .body(petData)
        .when()
            .post("/owners/" + flowOwnerId + "/pets/new")
        .then()
            .statusCode(302);

        // Step 3: Verify owner has pet
        given()
            .accept(ContentType.HTML)
        .when()
            .get("/owners/" + flowOwnerId)
        .then()
            .statusCode(200)
            .body(containsString("CompleteFlowPet"))
            .body(containsString("Complete"))
            .body(containsString("FlowTest"));
    }

    @Test
    @DisplayName("E2E: Pet names are case-insensitive for duplicates")
    void addPet_caseInsensitiveDuplicateName_shouldFail() {
        Assumptions.assumeTrue(testOwnerId != null);

        // Try to add pet with same name but different case
        String duplicateData = "name=fluffy&birthDate=2021-01-01&type=snake";

        given()
            .contentType(ContentType.URLENC)
            .body(duplicateData)
        .when()
            .post("/owners/" + testOwnerId + "/pets/new")
        .then()
            .statusCode(200)
            .body(containsString("already exists"));
    }
}
