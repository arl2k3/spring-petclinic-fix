package ec.edu.epn.petclinic.owner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private PetTypeRepository petTypeRepository;

    @Mock
    private PetValidator petValidator;

    @Mock
    private Clock clock;

    private PetController petController;

    private Owner owner;

    @BeforeEach
    void setup() {
        petController = new PetController(ownerRepository, petTypeRepository, petValidator, clock);
        mockMvc = MockMvcBuilders.standaloneSetup(petController)
            .setValidator(petValidator)
            .build();
        owner = buildOwnerWithPet();
        when(ownerRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(petTypeRepository.findPetTypes()).thenReturn(List.of(buildPetType(1, "dog")));
        when(petValidator.supports(any())).thenReturn(true);
        doAnswer(invocation -> null).when(petValidator).validate(any(), any());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(Instant.now());
    }

    @Test
    @DisplayName("Should_showCreationForm_When_InitCreation")
    void should_showCreationForm_When_InitCreation() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/pets/new", owner.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    @DisplayName("Should_rejectDuplicateName_When_CreatingPet")
    void should_rejectDuplicateName_When_CreatingPet() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", owner.getId())
                .param("name", "Lucky")
                .param("birthDate", LocalDate.now().toString())
                .param("type", "dog"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("pet", "name"))
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    @DisplayName("Should_rejectFutureBirthDate_When_CreatingPet")
    void should_rejectFutureBirthDate_When_CreatingPet() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        mockMvc.perform(post("/owners/{ownerId}/pets/new", owner.getId())
                .param("name", "Rocky")
                .param("birthDate", futureDate.toString())
                .param("type", "dog"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("pet", "birthDate"))
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    @DisplayName("Should_redirectAfterCreation_When_ValidPet")
    void should_redirectAfterCreation_When_ValidPet() throws Exception {
        when(ownerRepository.save(any(Owner.class))).thenReturn(owner);

        mockMvc.perform(post("/owners/{ownerId}/pets/new", owner.getId())
                .param("name", "Rocky")
                .param("birthDate", LocalDate.now().minusDays(1).toString())
                .param("type", "dog"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owners/7"))
            .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("Should_rejectDuplicateName_When_UpdatingPet")
    void should_rejectDuplicateName_When_UpdatingPet() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", owner.getId(), 15)
                .param("id", "15")
                .param("name", "Lucky")
                .param("birthDate", LocalDate.now().minusDays(2).toString())
                .param("type", "dog"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("pet", "name"))
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    @DisplayName("Should_rejectFutureBirthDate_When_UpdatingPet")
    void should_rejectFutureBirthDate_When_UpdatingPet() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(5);

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", owner.getId(), 15)
                .param("id", "15")
                .param("name", "Rocky")
                .param("birthDate", futureDate.toString())
                .param("type", "dog"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("pet", "birthDate"))
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    @DisplayName("Should_redirectAfterUpdate_When_PetValid")
    void should_redirectAfterUpdate_When_PetValid() throws Exception {
        when(ownerRepository.save(any(Owner.class))).thenReturn(owner);

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", owner.getId(), 15)
                .param("id", "15")
                .param("name", "Rocky")
                .param("birthDate", LocalDate.now().minusDays(1).toString())
                .param("type", "dog"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owners/7"))
            .andExpect(flash().attributeExists("message"));
    }

    private Owner buildOwnerWithPet() {
        Owner newOwner = new Owner();
        newOwner.setId(7);
        newOwner.setFirstName("George");
        newOwner.setLastName("Franklin");
        newOwner.setAddress("110 W. Liberty St.");
        newOwner.setCity("Madison");
        newOwner.setTelephone("6085551023");

        Pet existingPet = new Pet();
        existingPet.setId(15);
        existingPet.setName("Lucky");
        existingPet.setBirthDate(LocalDate.now().minusYears(1));
        existingPet.setType(buildPetType(2, "cat"));
        newOwner.getPetsInternal().add(existingPet);
        return newOwner;
    }

    private PetType buildPetType(int id, String name) {
        PetType type = new PetType();
        type.setId(id);
        type.setName(name);
        return type;
    }
}
