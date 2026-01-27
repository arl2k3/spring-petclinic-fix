package ec.edu.epn.petclinic.system;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Runtime exception to indicate that an entity was not found.
 * Mapped to HTTP 404 when thrown from a controller.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " not found with id: " + id);
    }
}
