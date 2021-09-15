package marathon;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="registerMasters", path="registerMasters")
public interface RegisterMasterRepository extends PagingAndSortingRepository<RegisterMaster, Long>{


}
