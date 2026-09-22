package br.com.confirmacao;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HexFormat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FlywayMigrationTest {
 @Test void appliedMigrationsRemainImmutable() throws Exception {
  assertEquals("a4abbb5e8d13b85e9a2096ad9c671657fcf47e751f133cbe0e1d80fb6f018758",hash("V14__add_professional_profile_preferences.sql"));
  assertEquals("2a40c9847cf8f0719bd085cb987d2e3a345cdc9895d2a4a39df34849dc0e5a33",hash("V15__whatsapp_reminder_preference.sql"));
 }
 private String hash(String name) throws Exception {
  try(InputStream input=getClass().getResourceAsStream("/db/migration/"+name)){
   if(input==null)throw new FileNotFoundException(name);
   String normalized=new String(input.readAllBytes(),StandardCharsets.UTF_8).replace("\r\n","\n");
   return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(normalized.getBytes(StandardCharsets.UTF_8)));
  }
 }
}
