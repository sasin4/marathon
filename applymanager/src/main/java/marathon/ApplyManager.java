package marathon;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="ApplyManager_table")
public class ApplyManager {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long applyId;
    private String applicantName;
    private String phoneNo;
    private String address;
    private String deliveryStatus;
    private String topSize;
    private String bottomSize;

    @PostPersist
    public void onPostPersist(){
        ApplySaved applySaved = new ApplySaved();
        BeanUtils.copyProperties(this, applySaved);
        applySaved.publishAfterCommit();

        ApplyCancelled applyCancelled = new ApplyCancelled();
        BeanUtils.copyProperties(this, applyCancelled);
        applyCancelled.publishAfterCommit();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getApplyId() {
        return applyId;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }
    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
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