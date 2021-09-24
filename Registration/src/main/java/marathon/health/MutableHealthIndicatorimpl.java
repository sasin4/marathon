package marathon.health;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class MutableHealthIndicatorimpl implements MutableHealthIndicator {

    private final AtomicReference<Health> healthRef = new AtomicReference<>(Health.up().build());

    @Override
    public Health health() {
        return healthRef.get();
    }

    @Override
    public void setHealth(Health health) {
        healthRef.set(health);
    }
}