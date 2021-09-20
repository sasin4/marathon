package marathon;

import javax.persistence.*;

@Entity
@Table(name="Dashboard_table")
public class Dashboard {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private long registerId;
        private String name;
        private String phoneNo;
        private String address;
        private String status;
        private String topSize;
        private String bottomSize;
        private Integer amount;
        private String deliveryStatus;
        private String payStatus;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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
        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }
        public String getDeliveryStatus() {
            return deliveryStatus;
        }

        public void setDeliveryStatus(String deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
        }
        public String getPayStatus() {
            return payStatus;
        }

        public void setPayStatus(String payStatus) {
            this.payStatus = payStatus;
        }

        public long getRegisterId() {
            return this.registerId;
        }

        public void setRegisterId(long registerId) {
            this.registerId = registerId;
        }

}