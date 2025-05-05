package com.example.payment.controller;

import com.example.payment.dto.OrderRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*") // Cho phép Angular gọi API từ domain khác
public class PaymentController {

    // MOMO
    @PostMapping("/create/momo")
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

    // VN-PAY
    @PostMapping("/create/vnpay")
    public ResponseEntity<?> createVNPayPayment(@RequestBody OrderRequest request, HttpServletRequest httpServletRequest) throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TmnCode = "I44T3L90"; // <-- từ email VNPay
        String vnp_HashSecret = "ET2RHUCFRKUX1WX5WER3YIE1A5M8NP9Z"; // <-- từ email VNPay
        String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        String vnp_ReturnUrl = "https://payment-test-nine.vercel.app/return"; // frontend return
//        String vnp_ReturnUrl = "http://localhost:4200/return"; // frontend return
//        String vnp_IpnUrl = "https://paymenttest-production-8156.up.railway.app/api/payment/notify"; // URL xử lý IPN trên server thực tế

        String orderId = UUID.randomUUID().toString().substring(0, 8);
        String vnp_OrderInfo = "Thanh toan don hang " + orderId;
        String vnp_OrderType = "other";
        String vnp_Amount = String.valueOf(request.getAmount() * 100); // Nhân 100 theo yêu cầu
        String vnp_Locale = "vn";
        String vnp_CurrCode = "VND";
        String vnp_IpAddr = httpServletRequest.getRemoteAddr();
        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_TxnRef", orderId);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
//        vnp_Params.put("vnp_IpnUrl", vnp_IpnUrl); // <-- thêm vào params gửi đi
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Sắp xếp theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String value = vnp_Params.get(fieldName);
            if ((value != null) && (!value.isEmpty())) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                query.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        String paymentUrl = vnp_Url + "?" + query.toString();
        return ResponseEntity.ok(Collections.singletonMap("redirectUrl", paymentUrl));
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac.init(secretKey);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while generating hash", e);
        }
    }

    // Xử lý notifyUrl (tùy chọn)
    @PostMapping("/notify")
    public ResponseEntity<String> paymentNotify(HttpServletRequest request) {
        try {
            // Lấy các tham số từ request
            Map<String, String[]> params = request.getParameterMap();
            String secureHash = params.get("vnp_SecureHash")[0]; // Chữ ký từ VNPay
            String computedSecureHash = computeSecureHash(params); // Tính lại chữ ký

            // Kiểm tra chữ ký
            if (secureHash.equals(computedSecureHash)) {
                // Xử lý giao dịch ở đây (Ví dụ: lưu vào database)
                return ResponseEntity.ok("Thanh toán thành công");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chữ ký không hợp lệ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi server");
        }
    }

    // tính toán chữ ký
    private String computeSecureHash(Map<String, String[]> params) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        StringBuilder rawData = new StringBuilder();

        // Sắp xếp các tham số theo thứ tự
        params.keySet().stream()
                .filter(key -> !key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType"))
                .sorted()
                .forEach(key -> {
                    String value = params.get(key)[0];
                    rawData.append(key).append("=").append(value).append("&");
                });

        // Loại bỏ dấu "&" cuối cùng
        rawData.deleteCharAt(rawData.length() - 1);

        // Thêm secretKey vào cuối chuỗi
        String data = rawData.toString() + "&" + "vnp_SecureHashSecret=" + "your_secret_key";

        // Tính toán chữ ký bằng HMAC-SHA256
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec("your_secret_key".getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hashBytes = sha256_HMAC.doFinal(data.getBytes("UTF-8"));

        // Chuyển đổi mảng byte thành chuỗi hexadecimal
        StringBuilder hashString = new StringBuilder();
        for (byte b : hashBytes) {
            hashString.append(String.format("%02x", b));
        }

        return hashString.toString().toUpperCase();
    }


}
