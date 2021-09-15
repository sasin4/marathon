package marathon;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="applyManagers", path="applyManagers")
public interface ApplyManagerRepository extends PagingAndSortingRepository<ApplyManager, Long>{


}
