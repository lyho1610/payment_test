package com.example.payment.controller;

import com.example.payment.dto.OrderRequest;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*") // Cho phép Angular gọi API từ domain khác
public class MomoPaymentController {

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody OrderRequest request) throws Exception {
        String endpoint = "https://test-payment.momo.vn/v2/gateway/api/create";
        String partnerCode = "MOMOXXXXXX";
        String accessKey = "your_access_key";
        String secretKey = "your_secret_key";

        String orderId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toán đơn hàng " + orderId;
        String redirectUrl = "http://localhost:4200/return";
        String ipnUrl = "http://localhost:8085/api/payment/notify";
        String amount = String.valueOf(request.getAmount());

        // Tạo chuỗi rawSignature
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=captureWallet";

        // Tạo chữ ký bằng HMAC SHA-256
        String signature = hmacSha256(secretKey, rawSignature);

        // Tạo payload để gửi đi
        Map<String, Object> payload = new HashMap<>();
        payload.put("partnerCode", partnerCode);
        payload.put("accessKey", accessKey);
        payload.put("requestId", requestId);
        payload.put("amount", amount);
        payload.put("orderId", orderId);
        payload.put("orderInfo", orderInfo);
        payload.put("redirectUrl", redirectUrl);
        payload.put("ipnUrl", ipnUrl);
        payload.put("extraData", "");
        payload.put("requestType", "captureWallet");
        payload.put("signature", signature);
        payload.put("lang", "vi");

        // Gửi yêu cầu POST tới MoMo API
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // Nhận phản hồi từ MoMo API
        ResponseEntity<Map> momoResponse = restTemplate.exchange(endpoint, HttpMethod.POST, entity, Map.class);
        return ResponseEntity.ok(momoResponse.getBody());
    }

    // Phương thức HMAC SHA-256
    public String hmacSha256(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes());
        return Hex.encodeHexString(hash);  // Chuyển đổi kết quả thành chuỗi hex
    }

    // Xử lý notifyUrl (tùy chọn)
    @PostMapping("/notify")
    public ResponseEntity<?> handleNotify(@RequestBody Map<String, Object> data) {
        System.out.println("Notify từ Momo: " + data);
        return ResponseEntity.ok("ok");
    }
}
