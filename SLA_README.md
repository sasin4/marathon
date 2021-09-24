#  lv2 SLA


## 사전 작업
- AWS 환경 설정 값
  - 리전코드     : ca-central-1
  - 사용자 계정  : user-dongjin
  - 클러스터     : user-dongjin-eks
  - 레포지터리   : 422489764856.dkr.ecr.ca-central-1.amazonaws.com
  - 접속 URL     : https://meuron-sk.signin.aws.amazon.com/console
  - 파일 시스템  : fs-9aaea577
  - AWS_ACCOUNT_ID : 422489764856
  - KUBE_URL : https://E1B761DB2E500713EA1BE5895042085D.gr7.ca-central-1.eks.amazonaws.com    (EKS -> user-dongjin-eks -> 구성 탭 -> 세부정보 -> API 서버 엔트포인트 복사)
  - KUBE_TOKEN :

<br>

### ◆ 클러스터 생성 및 kubctl 연결 및 docker 연결.
- IAM 보안 자격증명 설정 (한번 설정한 경우 다시 설정필요 없음) (Container-Orchestration(AWS)_2021.pdf – P59 ~P61 참조)
```
> aws configure
AWS Access Key ID [None]: 
AWS Secret Access Key [None]: 
Default region name [None]: ca-central-1
Default output format [None]: json
```

- AWS 클러스터 생성(EKS)  (한번 생성한 경우 다시 생성 필요 없음) (생성에 시간 오래 걸림 10분)
```
> eksctl create cluster --name user-dongjin-eks --version 1.19 --nodegroup-name standard-workers --node-type t3.medium --nodes 4 --nodes-min 1 --nodes-max 4
```
- - 클러스터 삭제시 
```
> eksctl delete cluster --region=ca-central-1 --name=user-dongjin-eks
```

- AWS 클러스터 토큰 가져오기
```
> aws eks --region ca-central-1 update-kubeconfig --name user-dongjin-eks
> kubectl get all
> kubectl config current-context
```

- AWS 컨테이너 레지스트리(ECR) 로그인
```
> docker login --username AWS -p $(aws ecr get-login-password --region ca-central-1) 422489764856.dkr.ecr.ca-central-1.amazonaws.com/
```

- Metric-Server 설치  (이미 설치한 경우에는 추가 설치 필요 없음)
```
> kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.5.0/components.yaml
> kubectl top node  (** 시간이 조금 걸림)
```


- Kafka 설치 (gatway를 제외하고는 각 서비스가 kafka에 연결하므로 kafka 서비스가 떠 있지 않으면 pod, deployment  올라오지 않음.)
  - helm 설치 필요.  (http://34.117.35.195/operation/checkpoint/check-point-two/ 참고)
```
> kubectl --namespace kube-system create sa tiller 
> kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller

> helm repo add incubator https://charts.helm.sh/incubator
> helm repo update
> kubectl create ns kafka
> helm install my-kafka --namespace kafka incubator/kafka --debug

> kubectl get svc my-kafka -n kafka
> kubectl get all -n kafka
```

- Kafka Listen  확인.
```
> kubectl exec -it my-kafka-0 -n kafka -- bash
> kafka-console-consumer --bootstrap-server my-kafka.kafka.svc.cluster.local:9092 --topic convenience --from-beginning
```
<br/>

### ◆ PVC (Persistant volume claim) 설정.
- EFS 생성
  - **교재-Container-Orchestration(AWS)_2021.pdf Page.132 참고**

-  ServerAccount 생성  (yaml 폴더에 관련 yaml 파일 있음.)
```
> kubectl apply -f https://raw.githubusercontent.com/djjoung/convenience/main/yaml/efs-sa.yaml
```
- SA(efs-provisioner)에 권한(rbac) 설정
```
> kubectl apply -f kubectl apply -f https://raw.githubusercontent.com/djjoung/convenience/main/yaml/efs-rbac.yaml
```

- provisioner 설치
  efs-provisioner-deploy.yaml 파일에서    ** 파일을 받아 수정 후 적용
  - value: #{efs system id} => 파일 시스템 ID
  - value: # {aws region} => EKS 리전
  - server: # {file-system-id}.efs.{aws-region}.amazonaws.com
```
> kubectl apply -f https://raw.githubusercontent.com/djjoung/convenience/main/yaml/efs-provisioner-deploy.yaml
> kubectl get pod

NAME                              READY   STATUS    RESTARTS   AGE
efs-provisioner-5976978f5-cqbzq   1/1     Running   0          19s
```
- storageClass 등록, 조회
```
> kubectl apply -f https://raw.githubusercontent.com/djjoung/convenience/main/yaml/efs-storageclass.yaml
> kubectl get sc
NAME            PROVISIONER             RECLAIMPOLICY   VOLUMEBINDINGMODE      ALLOWVOLUMEEXPANSION   AGE
aws-efs         my-aws.com/aws-efs      Delete          Immediate              false                  14s
gp2 (default)   kubernetes.io/aws-ebs   Delete          WaitForFirstConsumer   false                  27h
```
- pvc 생성
```
> kubectl apply -f https://raw.githubusercontent.com/djjoung/convenience/main/yaml/volume-pvc.yaml
> kubectl get pvc
> kubectl describe pvc
  Type    Reason                 Age                From                                                                                     Message
  ----    ------                 ----               ----                                                                                     -------
  Normal  ExternalProvisioning   35s (x2 over 35s)  persistentvolume-controller                                                              waiting for a volume to be created, either by external provisioner "my-aws.com/aws-efs" or manually created by system administrator
  Normal  Provisioning           35s                my-aws.com/aws-efs_efs-provisioner-5976978f5-cqbzq_5cde0b7c-906d-477e-9e02-5b4823a9ca5c  External provisioner is provisioning volume for claim "default/aws-efs"
  Normal  ProvisioningSucceeded  35s                my-aws.com/aws-efs_efs-provisioner-5976978f5-cqbzq_5cde0b7c-906d-477e-9e02-5b4823a9ca5c  Successfully provisioned volume pvc-c770d8b7-ef09-4a19-903b-cced4daa9f1d
```

<br/>
<br/>

---
## 마이크로서비스 배포 (CodeBuild)
### ◆ ECR(Elastic Container Repositoy) 생성  (주의 ECR의 레파지토리 명에 "_", 대문자가 들어가면  Repository 생성시  Error가 발생 함.)
*아래의 'user-dongjin-xxxxxx' 의 레파지토리 명은 필요한 이름으로 변경.*

- gateway 서비스
```
> aws ecr create-repository --repository-name user-dongjin-gateway --region ca-central-1
```
- Reservation 서비스
```
> aws ecr create-repository --repository-name user-dongjin-reservation --region ca-central-1
```

-	Pay 서비스
```
> aws ecr create-repository --repository-name user-dongjin-pay --region ca-central-1
```

-	Store 서비스
```
> aws ecr create-repository --repository-name user-dongjin-store --region ca-central-1
```

-	Supplier 서비스
```
> aws ecr create-repository --repository-name user-dongjin-supplier --region ca-central-1
```

- View 서비스
```
> aws ecr create-repository --repository-name user-dongjin-view --region ca-central-1
```

- 레포지터리 생성 확인

![image](https://user-images.githubusercontent.com/22004206/132270281-d9f0154e-ba48-442f-90f2-9208b6d1886e.png)

<br/>

### ◆ CodeBuild 설정 (AWS-CICD-Pipeline_2021.pdf, P37 ~ P53 순서대로 따라 작성하면됨. )
- 생성 할 CodeBuild
  - gateway
  - Reservation
  - Pay
  - Store
  - Supplier
  - View

### ◆ github의 buildspec-kubectl.yaml 수정
각 service의 buildspec-kubectl.yaml 에서 line5.6 값 이미지 Name에 맞게 수정.
- codebuild 설정시 각 서비스 별로 아래와 같은 형식으로 경로를 넣어 줘야함.
![image](https://user-images.githubusercontent.com/22004206/132272338-147fd66b-1ffc-4ce0-8472-3c626510bac6.png)




** 교재에 나와 있는 buildspec.yml은 사용하지 않는다.

![image](https://user-images.githubusercontent.com/22004206/132152084-f97970db-72dc-4891-82d3-43c81564de24.png)

- CodeBuild 설정시 사용될 환경변수  (각 서비스 마다 설정 필요)
  - AWS_ACCOUNT_ID
  - KUBE_URL
  - KUBE_TOKEN    (아래 **KUBE_TOKEN 을 얻기위한 설정** 참조 )


### ◆ CodeBuild 에서 EKS 연결 
  쿠버네티스에 접속하기 위해서는 쿠버네티스API주소 와 클러스터 어드민 권한이 있는 토큰이 있으면 접속 가능.
( 실제로 kubectl 명령어가 두개의 조합으로 이루어 져 있음 )
- KUBE_URL - Kubernates API 주소 위치

 ![image](https://user-images.githubusercontent.com/22004206/132151356-8eba5e10-e5dc-40f1-8d64-8746f049e338.png)

- KUBE_TOKEN 을 얻기위한 설정
  - Service Account 생성
```
> cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  name: eks-admin
  namespace: kube-system
EOF
```

-  -	ClusterRoleBinding 생성
```
> cat <<EOF | kubectl apply -f -
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  name: eks-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: eks-admin
  namespace: kube-system
EOF
```

-  -	SA로 EKS 접속 토큰 가져오기
```
> kubectl -n kube-system describe secret eks-admin
[#token 결과값만 공백없이 복사]
```

### ◆ CodeBuild와 ECR 연결정책 JSON (각 서비스 마다 설정 필요) * 정책명 ex) user-dongjin-reservation-codebuild-policy)
상세 내용은 교재 참고

![image](https://user-images.githubusercontent.com/22004206/132272697-10c16c54-d154-4ac1-b959-92e231807da8.png)


```
{
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:CompleteLayerUpload",
        "ecr:GetAuthorizationToken",
        "ecr:InitiateLayerUpload",
        "ecr:PutImage",
        "ecr:UploadLayerPart"
      ],
      "Resource": "*",
      "Effect": "Allow"
 }
 ```



### ◆ 서비스 확인
-	배포 Service  확인
```
> kubectl get all
```


## **Circuit Breaker**
- 시나리오
  1. 예약(reservation) --> 결재(pay)시의 연결을 RESTful Request/Response 로 연동하여 구현 함. 결제 요청이 과도할 경우 CB가 발생하고 fallback으로 결재 지연 메새지를 보여줌으로 장애 격리 시킴.
  2. circuit break의 timeout은 610mm 설정. 
  3. Pay 서비스에 임의의 부하 처리.
  4. 부하테스터(seige) 를 통한 circuit break 확인. 
    - 결재 지연 메세지 확인.
    - seige의 Availability 100% 확인.

- 서킷브레이커 설정
  - Reservation 서비스의 application.yaml에 아래 내용 추가
```
feign:
  hystrix:
    enabled: true

hystrix:
  command:
    # 전역설정 timeout이 610ms 가 넘으면 CB 처리.
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610
```
- Pay 서비스에 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게
  - PayHistoryController.java에 아래 코드 추가
```
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
```
- Resevation 서비스의  FeignClient fallback 옵션 추가.
  - PayHistoryService.java에 아래 코드 추가.
```
@FeignClient(name ="delivery", url="${api.url.pay}", fallback = PayHistoryServiceImpl.class)
```

- 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
  - 동시사용자 100명
  - 60초 동안 실시
  - Reservation 서비스의 log 확인.
```
siege -c100 -t30S --content-type "application/json" 'http://reservation:8080/reservation/order POST {"productId":1,"productName":"Galaxy","productPrice":125000,"customerId":99,"customerName":"Anderson","customerPhone":"010-1234-5678","qty":1}'


** SIEGE 4.1.1
** Preparing 100 concurrent users for battle.
The server is now under siege...
HTTP/1.1 201     2.19 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.20 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.20 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.20 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.20 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.21 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.21 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.21 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.22 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.22 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.66 secs:     378 bytes ==> POST http://reservation:8080/reservation/order




Lifting the server siege...
Transactions:		        8776 hits
Availability:		      100.00 %
Elapsed time:		       29.83 secs
Data transferred:	        1.67 MB
Response time:		        0.34 secs
Transaction rate:	      294.20 trans/sec
Throughput:		        0.06 MB/sec
Concurrency:		       99.32
Successful transactions:        8776
Failed transactions:	           0
Longest transaction:	        2.24
Shortest transaction:	        0.00

```
 
```
@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@
@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@
@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@
########## 결제가 실패하였습니다 ############
```
- 부하 테스트에서 100% 가 성공함. CB 발생시 FeinClient의 Fallback이 동작하여 결재 지연 메세지를 고객에게 보여줌 으로 다른 동작 하지 못하게 함. 

<br/>

---
## **오토스케일 아웃 (HorizontalPodAutoscaler)**
- 예약서비스(Reservation)에 대해  CPU Load 50%를 넘어서면 Replica를 10까지 늘려준다. 
  - buildspec-kubectl.yaml
```
          cat <<EOF | kubectl apply -f -
          apiVersion: autoscaling/v2beta2
          kind: HorizontalPodAutoscaler
          metadata:
            name: reservation-hpa
          spec:
            scaleTargetRef:
              apiVersion: apps/v1
              kind: Deployment
              name: $_POD_NAME
            minReplicas: 1
            maxReplicas: 10
            metrics:
            - type: Resource
              resource:
                name: cpu
                target:
                  type: Utilization
                  averageUtilization: 50
          EOF
```

- 예약서비스(Reservation)에 대한 CPU Resouce를 500m으로 제한 한다.
  - buildspec-kubectl.yaml
```
                    resources:
                      limits:
                        cpu: 500m
                        memory: 500Mi
                      requests:
                        cpu: 200m
                        memory: 300Mi
```

- Siege (로더제너레이터)를 설치하고 해당 컨테이너로 접속한다.
```
> kubectl create deploy siege --image=ghcr.io/acmexii/siege-nginx:latest
> kubectl exec pod/[SIEGE-POD객체] -it -- /bin/bash
```

- 예약 서비스에 워크로드를 10초 동안 걸어준다.
```
siege -c100 -t10S --content-type "application/json" 'http://reservation:8080/reservation/hpa'
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다 : 각각의 Terminal에 
  - 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다:  
```
> kubectl get deploy reservation -w

NAME          READY   UP-TO-DATE   AVAILABLE   AGE
reservation   1/1     1            1           63m
reservation   1/3     1            1           63m
reservation   1/3     1            1           63m
reservation   1/3     1            1           63m
reservation   1/3     3            1           63m
:


> watch -n 1 kubectl top po
NAME                                 READY   STATUS    RESTARTS   AGE   IP               NODE                                              NOMINATED NODE   READINESS GATES
pod/efs-provisioner-77c568c8-pmkxc   1/1     Running   0          16h   192.168.13.208   ip-192-168-5-42.ca-central-1.compute.internal     <none>           <none>
pod/gateway-564d85fbc4-dbhht         1/1     Running   0          70m   192.168.19.153   ip-192-168-5-42.ca-central-1.compute.internal     <none>           <none>
pod/pay-666cf5c795-blfqk             1/1     Running   0          31m   192.168.32.153   ip-192-168-61-25.ca-central-1.compute.internal    <none>           <none>
pod/reservation-779f5585bc-6bdxg     1/1     Running   0          31m   192.168.28.44    ip-192-168-5-42.ca-central-1.compute.internal     <none>           <none>
pod/reservation-779f5585bc-hgjl9     0/1     Running   0          37s   192.168.52.66    ip-192-168-61-25.ca-central-1.compute.internal    <none>           <none>
pod/reservation-779f5585bc-rshlh     0/1     Running   0          37s   192.168.95.48    ip-192-168-73-205.ca-central-1.compute.internal   <none>           <none>
pod/siege-pvc                        1/1     Running   0          16h   192.168.1.22     ip-192-168-20-33.ca-central-1.compute.internal    <none>           <none>


> watch -n 1 kubectl get all -o wide 
NAME                             CPU(cores)   MEMORY(bytes)
efs-provisioner-77c568c8-pmkxc   1m           10Mi
gateway-564d85fbc4-dbhht         7m           150Mi
pay-666cf5c795-blfqk             6m           254Mi
reservation-779f5585bc-6bdxg     4m           280Mi
reservation-779f5585bc-hgjl9     487m         154Mi
reservation-779f5585bc-rshlh     483m         159Mi
siege-pvc                        0m           6Mi
store-7f9f99dbfc-tfsvr           5m           258Mi
supplier-696bb6f7dd-xdpkc        5m           262Mi
view-bdf94d47d-shvwc             4m           279Mi

```
---

---
## **Liveness & Readiness**
### **◆ Liveness- HTTP Probe**
- 시나리오
  1. Reservation 서비스의 Liveness 설정을 확인힌다. 
  2. Reservation 서비스의 Liveness Probe는 actuator의 health 상태 확인을 설정되어 있어 actuator/health 확인.
  3. pod의 상태 모니터링
  4. Reservation 서비스의 Liveness Probe인 actuator를 down 시켜 Reservation 서비스가 termination 되고 restart 되는 self healing을 확인한다. 
  5. Reservation 서비스의 describe를 확인하여 Restart가 되는 부분을 확인한다.

<br/>

- Reservation 서비스의 Liveness probe 설정 확인
```
kubectl get deploy reservation -o yaml

                  :
        livenessProbe:
          failureThreshold: 5
          httpGet:
            path: /actuator/health
            port: 8080
            scheme: HTTP
          initialDelaySeconds: 120
          periodSeconds: 5
          successThreshold: 1
          timeoutSeconds: 2
                  :
```

- Httpie를 사용하기 위해 Siege를 설치하고 해당 컨테이너로 접속한다.
```
> kubectl create deploy siege --image=ghcr.io/acmexii/siege-nginx:latest
> kubectl exec pod/[SIEGE-POD객체] -it -- /bin/bash
```

- Liveness Probe 확인 
```
> http http://reservation:8080/actuator/health      # Liveness Probe 확인

HTTP/1.1 200 
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Tue, 07 Sep 2021 14:58:15 GMT
Transfer-Encoding: chunked

{
    "status": "UP"
}
```

- Liveness Probe Fail 설정 및 확인 
  - Reservation Liveness Probe를 명시적으로 Fail 상태로 전환한다.
```
> http DELETE http://reservation:8080/healthcheck    #actuator health 를 DOWN 시킨다.
> http http://reservation:8080/actuator/health
HTTP/1.1 503 
Connection: close
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Wed, 08 Sep 2021 01:56:07 GMT
Transfer-Encoding: chunked

{
    "status": "DOWN"
}
```

- Probe Fail에 따른 쿠버네티스 동작확인  
  - Reservation 서비스의 Liveness Probe가 /actuator/health의 상태가 DOWN이 된 것을 보고 restart를 진행함. 
  아래 커맨드를 통해 RESTARTS가 1로 바뀐것을 확인. 
  describe 를 통해 해당 pod가 restart 된 것을 알 수 있다.
```
> kubectl get pod
NAME                          READY   STATUS    RESTARTS
gateway-5587878c8c-7rhx8      1/1     Running   0          8m26s
pay-657d6ff8f5-wvmxs          1/1     Running   0          8m24s
reservation-dc4ff786c-bxp6m   1/1     Running   1          8m23s
siege-75d5587bf6-8xnmc        1/1     Running   0          6m31s
store-6486b7565b-txjjr        1/1     Running   0          8m23s
supplier-9bc6bc8b5-m4l8m      1/1     Running   0          8m23s

> kubectl describe pod/[ORDER-POD객체]
Events:
  Type     Reason     Age                  From               Message
  ----     ------     ----                 ----               -------
  Normal   Scheduled  21m                  default-scheduler  Successfully assigned default/reservation-dc4ff786c-bxp6m to ip-192-168-50-127.ca-central-1.compute.internal
  Normal   Pulling    21m                  kubelet            Pulling image "422489764856.dkr.ecr.ca-central-1.amazonaws.com/user-dongjin-reservation:6a6573b58027490f3d56be72e85d445d6da87746"
  Normal   Pulled     21m                  kubelet            Successfully pulled image "422489764856.dkr.ecr.ca-central-1.amazonaws.com/user-dongjin-reservation:6a6573b58027490f3d56be72e85d445d6da87746" in 1.323451813s
  Normal   Killing    15m                  kubelet            Container reservation failed liveness probe, will be restarted
  Normal   Created    15m (x2 over 21m)    kubelet            Created container reservation
  Normal   Started    15m (x2 over 21m)    kubelet            Started container reservation
  Normal   Pulled     15m                  kubelet            Container image "422489764856.dkr.ecr.ca-central-1.amazonaws.com/user-dongjin-reservation:6a6573b58027490f3d56be72e85d445d6da87746" already present on machine
  Warning  Unhealthy  14m (x4 over 21m)    kubelet            Readiness probe failed: Get "http://192.168.37.58:8080/actuator/health": dial tcp 192.168.37.58:8080: connect: connection refused
  Warning  Unhealthy  4m41s (x8 over 15m)  kubelet            Liveness probe failed: HTTP probe failed with statuscode: 503
  Warning  Unhealthy  4m36s (x8 over 15m)  kubelet            Readiness probe failed: HTTP probe failed with statuscode: 503
```

### **◆ Rediness- HTTP Probe** 
- 시나리오
  1. 현재 구동중인 Reservation 서비스에 길게(3분) 부하를 준다. 
  2. pod의 상태 모니터링
  3. AWS에 CodeBuild에 연결 되어있는 github의 코드를 commit한다.
  4. Codebuild를 통해 새로운 버전의 Reservation이 배포 된다. 
  5. pod 상태 모니터링에서 기존 Reservation 서비스가 Terminating 되고 새로운 Reservation 서비스가 Running하는 것을 확인한다.
  6. Readness에 의해서 새로운 서비스가 정상 동작할때까지 이전 버전의 서비스가 동작하여 seieg의 Avality가 100%가 된다.

<br/>

- 현재 구동중인 Reservation 서비스에 길게(3분) 부하를 준다. 
  - Siege (로더제너레이터)를 설치하고 해당 컨테이너로 접속한다.
```
> kubectl create deploy siege --image=ghcr.io/acmexii/siege-nginx:latest    # 이미 생성이 된 경우 무시.
> kubectl exec pod/[SIEGE-POD객체] -it -- /bin/bash
> siege -v -c1 -t120S http://reservation:8080/reservations
```

- pod의 상태 모니터링
```
> watch -n 1 kubectl get pod    ==> pod가 생성되고 소멸되는 과정 확인.

NAME                          READY   STATUS    RESTARTS   AGE
gateway-6bdf6cf865-n4b8v      1/1     Running   0          15m
pay-5bdf5998d9-qpdtk          1/1     Running   0          14m
reservation-c544fd6bd-47sm5   1/1     Running   0          13m
siege-75d5587bf6-8xnmc        1/1     Running   0          93m
store-546b7cd7c8-gghdv        1/1     Running   0          15m
supplier-6477564dd4-tq9tt     1/1     Running   0          14m    
```

- AWS에 CodeBuild에 연결 되어있는 github의 코드를 commit한다.
  Resevatio 서비스의 아무 코드나 수정하고 commit 한다. 
  배포 될때까지 잠시 기다린다. 
  Ex) buildspec-kubectl.yaml에 carrage return을  추가 commit 한다. 



- pod 상태 모니터링에서 기존 Reservation 서비스가 Terminating 되고 새로운 Reservation 서비스가 Running하는 것을 확인한다.
```
Every 1.0s: kubectl get pod                                                                                                                                   jeongdongjin-ui-Macmini.local: Wed Sep  8 12:19:16 2021

NAME                           READY   STATUS    RESTARTS   AGE
gateway-5c7f47c9c5-z5slx       0/1     Running   0          11s
gateway-6bdf6cf865-n4b8v       1/1     Running   0          20m
pay-5bdf5998d9-qpdtk           1/1     Running   0          19m
pay-797f74998c-wh94q           0/1     Running   0          9s
reservation-585667dc8c-wlmtb   0/1     Running   0          8s
reservation-c544fd6bd-47sm5    1/1     Running   0          18m
siege-75d5587bf6-8xnmc         1/1     Running   0          98m
store-546b7cd7c8-gghdv         1/1     Running   0          20m
store-774c6757bd-gh5hx         0/1     Running   0          10s
supplier-6477564dd4-tq9tt      1/1     Running   0          19m
supplier-7bc4ff789d-qgkwk      0/1     Running   0          9s
```

- Readness에 의해서 새로운 서비스가 정상 동작할때까지 이전 버전의 서비스가 동작하여 seieg의 Avality가 100%가 된다.
```
Lifting the server siege...
Transactions:		       18572 hits
Availability:		      100.00 %
Elapsed time:		      119.79 secs
Data transferred:	        6.62 MB
Response time:		        0.01 secs
Transaction rate:	      155.04 trans/sec
Throughput:		        0.06 MB/sec
Concurrency:		        0.95
Successful transactions:       18572
Failed transactions:	           0
Longest transaction:	        0.68
Shortest transaction:	        0.00
```

## **PVC (Persistant volume claim)** 

- 시나리오
  1. bash shell을 사용할 수 있는 pod를 동일한 PVC 사용할 수 있게 설정 후 배포
  2. 각 서비스에서 생성한 파일을 확인. 
  
  <br/>
- 각 Deployment의 PVC 생성정보는 buildspec-kubeclt.yaml에 적용되어있다.
```
                    volumeMounts:
                      - mountPath: "/mnt/aws"
                        name: volume
                        :
                        :
                        :
                volumes:
                  - name: volume
                    persistentVolumeClaim:
                      claimName: aws-efs
```


- 각 서비스의 Event 발생시 JSON 정보를 파일로 저장한다. 마지막 정보만 저정하기 위해 Overwirte하여 저장한다. 
  - 아래와 같은 코드를 통하여 /mnt/aws의 경로에 저장한다. 
```
AbstractEvent.java

    // PVC Test
    public void saveJasonToPvc(String strJson){
        File file = new File("/mnt/aws/xxxxxxxxed_json.txt");

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(strJson);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
```

- 각 서비스에서 저장한 Event 정보파일을 동일한 PVC를 사용하는 Pod를 생성하여 배포 후 /mnt/aws에 저장되어 있는지 확인. 
```
> kubectl apply -f kubectl apply -f https://raw.githubusercontent.com/djjoung/convenience/main/yaml/pod-with-pvc.yaml
> kubectl get pod
> kubectl describe pod reservation
> kubectl exec -it seieg -- /bin/bash
> ls -al /mnt/aws

total 20
drwxrws--x 2 root 2000 6144 Sep 15 14:39 .
drwxr-xr-x 1 root root   17 Sep 15 12:33 ..
-rw-r--r-- 1 root 2000  154 Sep 15 14:37 payCancelled_json.txt
-rw-r--r-- 1 root 2000   99 Sep 15 14:29 productDelivered_json.txt
-rw-r--r-- 1 root 2000  158 Sep 15 14:36 productPickedupjson.txt
-rw-r--r-- 1 root 2000   90 Sep 15 14:37 productReserved_json.txt

```
- 서비스 Event를 저장한 파일들을 확인 할 수 있다. 
<br/>
<br/>