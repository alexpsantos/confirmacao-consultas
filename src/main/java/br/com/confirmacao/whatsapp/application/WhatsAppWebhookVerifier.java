package br.com.confirmacao.whatsapp.application;

import br.com.confirmacao.whatsapp.config.WhatsAppProperties;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class WhatsAppWebhookVerifier {
    private final WhatsAppProperties properties;
    public WhatsAppWebhookVerifier(WhatsAppProperties properties) { this.properties=properties; }
    public boolean validChallenge(String mode,String token){return "meta".equals(properties.provider())&&"subscribe".equals(mode)&&secureEquals(properties.meta().webhookVerifyToken(),token);}
    public boolean validSignature(byte[] payload,String signature){
        if(!"meta".equals(properties.provider())||blank(properties.meta().appSecret())||signature==null||!signature.startsWith("sha256="))return false;
        try { var mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(properties.meta().appSecret().getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return MessageDigest.isEqual(hex(mac.doFinal(payload)).getBytes(StandardCharsets.US_ASCII),signature.substring(7).getBytes(StandardCharsets.US_ASCII)); }
        catch(Exception e){return false;}
    }
    private boolean secureEquals(String left,String right){return !blank(left)&&right!=null&&MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8),right.getBytes(StandardCharsets.UTF_8));}
    private boolean blank(String value){return value==null||value.isBlank();}
    private String hex(byte[] value){var out=new StringBuilder();for(byte b:value)out.append(String.format("%02x",b));return out.toString();}
}
