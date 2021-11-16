package de.jpx3.intave.resource;

import de.jpx3.intave.annotate.Native;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public final class EncryptionLayer implements Resource {
  private final Resource target;

  public EncryptionLayer(Resource target) {
    this.target = target;
  }

  @Override
  public boolean available() {
    return target.available();
  }

  @Override
  public long lastModified() {
    return target.lastModified();
  }

  @Override
  @Native
  public void write(InputStream inputStream) {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byte[] buf = new byte[4096];
      int i;
      while ((i = inputStream.read(buf)) != -1) {
        byteArrayOutputStream.write(buf, 0, i);
      }
      inputStream.close();
      SecureRandom secureRandom = new SecureRandom();
      byte[] iv = new byte[12];
      secureRandom.nextBytes(iv);
      KeySpec spec = new PBEKeySpec("adXUOhsZW7H5m4dlOyrNV7ZvHBBB071Sy2jCiuUZ91QMAcYyexjxwDQmXL1LR1nV".toCharArray(), iv, 65536, 128); // AES-128
      SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
      SecretKey secretKey = new SecretKeySpec(key, "AES");
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
      byte[] encryptedData = cipher.doFinal(byteArrayOutputStream.toByteArray());
      byteArrayOutputStream.close();
      ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + encryptedData.length);
      byteBuffer.putInt(iv.length);
      byteBuffer.put(iv);
      byteBuffer.put(encryptedData);
      target.write(new ByteArrayInputStream(byteBuffer.array()));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  @Override
  @Native
  public InputStream read() {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      InputStream inputStream = target.read();
      byte[] buf = new byte[4096];
      int i;
      while ((i = inputStream.read(buf)) != -1) {
        byteArrayOutputStream.write(buf, 0, i);
      }
      inputStream.close();
      ByteBuffer byteBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
      byte[] iv = new byte[byteBuffer.getInt()];
      byteBuffer.get(iv);
      KeySpec spec = new PBEKeySpec("adXUOhsZW7H5m4dlOyrNV7ZvHBBB071Sy2jCiuUZ91QMAcYyexjxwDQmXL1LR1nV".toCharArray(), iv, 65536, 128); // AES-128
      SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
      SecretKey secretKey = new SecretKeySpec(key, "AES");
      byte[] cipherBytes = new byte[byteBuffer.remaining()];
      byteBuffer.get(cipherBytes);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
      return new ByteArrayInputStream(cipher.doFinal(cipherBytes));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return null;
  }

  @Override
  public void delete() {
    target.delete();
  }
}
