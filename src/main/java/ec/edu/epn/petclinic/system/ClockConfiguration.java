package ec.edu.epn.petclinic.system;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to provide a Clock bean for dependency injection.
 */
@Configuration
public class ClockConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
