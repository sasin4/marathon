package marathon.external;

public class PayServiceFallback implements PayService{
    @Override
    public void payRequest(Pay pay) {
        System.out.println("Circuit breaker has been opened. Fallback returned instead.");
    }
}