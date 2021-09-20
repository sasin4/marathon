package marathon;

import org.springframework.data.repository.CrudRepository;

public interface DashboardRepository extends CrudRepository<Dashboard, Long> {
    Dashboard findByregisterId(long Id);
}