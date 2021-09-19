package marathon;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="RegisterMaster_table")
public class RegisterMaster {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long registerId;
    private String name;
    private String phoneNo;
    private String address;
    private String deliveryStatus;
    private String topSize;
    private String bottomSize;

    @PostPersist
    public void onPostPersist(){
        System.out.println("############################## RegisterMaster PostPersist");
        RegisterComplete registerComplete = new RegisterComplete();
        BeanUtils.copyProperties(this, registerComplete);
        registerComplete.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate() {
        System.out.println("############################## RegisterMaster PostUpdate");
    	if (this.deliveryStatus.equals("CANCEL")) {    
            RegisterRemoved registerRemoved = new RegisterRemoved();
            BeanUtils.copyProperties(this, registerRemoved);
            registerRemoved.publishAfterCommit();

    	}
    }     

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getRegisterId() {
        return registerId;
    }

    public void setRegisterId(Long registerId) {
        this.registerId = registerId;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
    public String getTopSize() {
        return topSize;
    }

    public void setTopSize(String topSize) {
        this.topSize = topSize;
    }
    public String getBottomSize() {
        return bottomSize;
    }

    public void setBottomSize(String bottomSize) {
        this.bottomSize = bottomSize;
    }




}