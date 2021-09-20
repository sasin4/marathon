package marathon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

 @RestController
 @RequestMapping("/registrations")
 public class RegistrationController {

	@Autowired
	RegistrationRepository registrationRepository;
	
	//@ApiOperation(value = "마라톤 등록하기")	
	@PostMapping("/register")
	public ResponseEntity<Registration> requestRegister(@RequestBody Registration registration) {
		registration.setStatus("REGISTERED");
		System.out.println("########################################## RegistrationController:/register");
		System.out.println("id : "+ registration.getId());
		System.out.println("name : "+ registration.getName());
		System.out.println("phoneNo : "+ registration.getPhoneNo());
		System.out.println("address : "+ registration.getAddress());
		System.out.println("registrationStatus : "+ registration.getStatus());
		System.out.println("topSize : "+ registration.getTopSize());
		System.out.println("bottomSize : "+ registration.getBottomSize());
		System.out.println("amount : "+ registration.getAmount());

		Registration savedRegistration = registrationRepository.save(registration);
		return ResponseEntity.ok(savedRegistration);
	}
	
	//@ApiOperation(value = "마라톤 등록 취소")
	@PatchMapping("/cancel/{id}")
	public ResponseEntity<Registration> cancelRegistration(@PathVariable Long id) {
		Registration registration = registrationRepository.findById(id).orElseThrow(null);
		registration.setStatus("CANCEL");
		Registration canceledRegistration = registrationRepository.save(registration);
		return ResponseEntity.ok(canceledRegistration);
	}

 }