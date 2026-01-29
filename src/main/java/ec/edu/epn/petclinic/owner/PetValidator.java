package ec.edu.epn.petclinic.owner;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * <code>Validator</code> for <code>Pet</code> forms.
 * <p>
 * We're not using Bean Validation annotations here because it is easier to
 * define such
 * validation rule in Java.
 * </p>
 */
@Component
public class PetValidator implements Validator {

	private static final String REQUIRED = "required";

	@Override
	public void validate(Object obj, Errors errors) {
		Pet pet = (Pet) obj;
		// Note: name is not required per business requirements
		// Name validation is handled by the controller for duplicate checking

		// type validation
		if (pet.isNew() && pet.getType() == null) {
			errors.rejectValue("type", REQUIRED, REQUIRED);
		}

		// birth date validation
		if (pet.getBirthDate() == null) {
			errors.rejectValue("birthDate", REQUIRED, REQUIRED);
		}
	}

	/**
	 * This Validator validates *just* Pet instances
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return Pet.class.isAssignableFrom(clazz);
	}

}
