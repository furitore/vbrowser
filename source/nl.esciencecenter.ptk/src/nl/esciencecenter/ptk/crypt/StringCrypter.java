/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.crypt;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;

import nl.esciencecenter.ptk.util.StringUtil;

//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

/**
 * String Encrypter/Decryptor class. 
 * Can also be used to encrypt/decrypt byte arrays.
 */
public class StringCrypter
{
    public static class EncryptionException extends Exception
    {
        private static final long serialVersionUID = 1L;

        public EncryptionException(Throwable t)
        {
            super(t);
        }
        
        public EncryptionException(String message, Throwable cause)
        {
            super(message,cause);
        }
    }

    public static enum CryptScheme
    {
        DESEDE_ECB_PKCS5("DESede","DESede/ECB/PKCS5Padding",24),
        DES_ECB_PKCS5("DES","DES/ECB/PKCS5Padding",16)
        ;
        
        private String schemeName;
        
        private String configString;
        
        private int minKeyLength; 
        
        private CryptScheme(String name,String configName,int minimalKeyLength)
        {
            this.schemeName=name; 
            this.configString=configName;
            this.minKeyLength=minimalKeyLength; 
        }
        
        public String getSchemeName()
        {
            return schemeName; 
        }
        
        public String getConfigString()
        {
            return configString; 
        }
        
        public int getMinimalKeyLength()
        {
            return minKeyLength;
        }
    }

    // ========================================================================
    // Class Constants  
    // ========================================================================
//    public static final String DES_ENCRYPTION_SCHEME = "DES";
//
//    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
//
//    public static final String DESEDE_ECB_PKCS5 = "DESede/ECB/PKCS5Padding"; 
//
//    public static final String DES_ECB_PKCS5 = "DES/ECB/PKCS5Padding"; 
    
    public static final String CHARSET_UTF8 = "UTF-8";

    public static Secret getAppKey1()
    {
        // 'legacy app key 1'
        return new Secret("123CSM34567890ENCRYPTIONC3PR4KEY5678901234567890".toCharArray());
    }
   
    // ========================================================================
    // Instance 
    // ========================================================================

    private KeySpec keySpec;

    private SecretKeyFactory keyFactory;

    private Cipher cipher;

    private Charset charSet;
    
    private MessageDigest keyHasher;

    /**
     * Whether to use the plain character bytes from a password string instead of a hashing function.
     * This option is for legacy applications.
     */ 
    private boolean usePlainCharBytes=false;
    
//    public StringEncrypter() throws EncryptionException
//    {
//        init(getDefaultKey(),DESEDE_ENCRYPTION_SCHEME, StringHasher.SHA_256,CHARSET_UTF8);
//    }

    public StringCrypter(Secret encryptionKey) throws EncryptionException
    {
        init(encryptionKey,CryptScheme.DESEDE_ECB_PKCS5,StringHasher.SHA_256, CHARSET_UTF8);
    }
    
    public StringCrypter(Secret encryptionKey, CryptScheme encryptionScheme) throws EncryptionException
    {
        init(encryptionKey,encryptionScheme,StringHasher.SHA_256,CHARSET_UTF8);
    }
    
    public StringCrypter(Secret encryptionKey, CryptScheme encryptionScheme,String keyHashingScheme, String charEncoding) throws EncryptionException
    { 
        init(encryptionKey,encryptionScheme,keyHashingScheme,charEncoding);
    }
    
    private void init(Secret encryptionKey,CryptScheme encryptionScheme,String keyHasherScheme,String charEncoding) throws EncryptionException
    {
        if (encryptionKey == null)
        {
            throw new IllegalArgumentException("Encryption key was null");
        }
        
//        // DESedeKeySpec might complain with a rather cryptic Exception otherwise 
//        if ((keyHasherScheme==null) && (encryptionKey.trim().length() < MINIMUM_CRYPT_KEY_LENGTH))
//        {
//            throw new IllegalArgumentException("Encryption key was less than "+MINIMUM_CRYPT_KEY_LENGTH+" characters");
//        }
        
        try
        {
            if (keyHasherScheme==null)
            {
                this.usePlainCharBytes=true; 
            }
            else
            {
                keyHasher= MessageDigest.getInstance(keyHasherScheme);
            }
            setCharacterEncoding(charEncoding);
            byte[] keyAsBytes = createKeyDigest(encryptionKey); 
            initKey(keyAsBytes,null,encryptionScheme);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
    }
    
    public void setUsePlainCharBytes(boolean value)
    {
        this.usePlainCharBytes=value;  
    }
    
    /** Use hashing to create an unsalted key digest from the password/passkey */ 
    public byte[] createKeyDigest(Secret password)
    {
        ByteBuffer bbuf=password.toByteBuffer(charSet); 
 
        // Legacy code, not recommended
        if (this.usePlainCharBytes)
            return bbuf.array(); 
        
        // return MD5 or SHA hash. 
        this.keyHasher.reset(); 
        this.keyHasher.update(bbuf);
        byte keyBytes[]=keyHasher.digest();
        
        return keyBytes;
    }

    protected void initKey(byte rawKey[],byte IV[],CryptScheme encryptionScheme) throws EncryptionException
    {
        try
        {
            // IV only needed for CBC, not ECB:
            // IvParameterSpec ivSpec=null;
            //          
            // if (IV!=null)
            //    ivSpec = new IvParameterSpec(IV); 

            if (encryptionScheme.equals(CryptScheme.DESEDE_ECB_PKCS5))
            {
                keySpec = new DESedeKeySpec(rawKey);
                keyFactory = SecretKeyFactory.getInstance(encryptionScheme.getSchemeName());
                cipher = Cipher.getInstance(encryptionScheme.configString);
            }
            else if (encryptionScheme.equals(CryptScheme.DES_ECB_PKCS5))
            {
                keySpec = new DESKeySpec(rawKey);
                keyFactory = SecretKeyFactory.getInstance(encryptionScheme.getSchemeName());
                cipher = Cipher.getInstance(encryptionScheme.configString);
            }
            else
            {
                throw new IllegalArgumentException("Encryption scheme not supported: " + encryptionScheme);
            }
            
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
        catch (NoSuchPaddingException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
        catch (InvalidKeyException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
    }
    
    /**
     * Specify which characters set is used to get the bytes from password Strings. 
     * Default is UTF-8  
     */ 
    public void setCharacterEncoding(String charSetStr) throws UnsupportedEncodingException
    {
        charSet=Charset.forName(charSetStr);
        
        if (charSet==null)
            throw new UnsupportedEncodingException("No such Character encoding:"+charSetStr);
    }
    
    /** @see #setCharacterEncoding(String) */ 
    public Charset getCharacterEncoding()
    {
        return this.charSet; 
    }

    /**
     * Encrypts String and returns encoded result as base64 encoded String. 
     * This increases the String size by ~33%.  
     */
    public String encryptString(String unencryptedString) throws EncryptionException
    {
        byte ciphertext[]=encrypt(unencryptedString);
        return StringUtil.base64Encode(ciphertext);
    }
    
    /**
     * Encrypts String and returns encoded result as bytes. 
     */
    public byte[] encrypt(String unencryptedString) throws EncryptionException
    {
        if (StringUtil.isWhiteSpace(unencryptedString))
        {
            throw new IllegalArgumentException("unencrypted string was null or contains only whitespace.");
        }
        try
        {
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cleartext = unencryptedString.getBytes(charSet);
            byte[] ciphertext = cipher.doFinal(cleartext);
            return ciphertext;
        }
        catch (InvalidKeySpecException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
        catch (InvalidKeyException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
        catch (IllegalBlockSizeException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
        catch (BadPaddingException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
    }

    /** Decrypt base64 encoded and encrypted String */ 
    public String decryptString(String base64String) throws EncryptionException
    {
        if (StringUtil.isWhiteSpace(base64String))
        {
            throw new IllegalArgumentException("Encrypted String was null or empty");
        }
        
        try
        {
            byte[] cleartext= StringUtil.base64Decode(base64String);
            
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, key);
            
            byte[] ciphertext = cipher.doFinal(cleartext);
            return newString(ciphertext);
        }
        catch (Exception e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
    }

    /** Decrypt base64 encoded and encrypted String */ 
    public String decryptHexEncodedString(String hexEncodedString) throws EncryptionException
    {
        if (StringUtil.isWhiteSpace(hexEncodedString))
        {
            throw new IllegalArgumentException("Encrypted String was null or empty. Must be hexadecimal encoded String.");
        }
        
        try
        {
            byte[] cleartext= StringUtil.parseBytesFromHexString(hexEncodedString); 
            
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, key);
            
            byte[] ciphertext = cipher.doFinal(cleartext);
            return newString(ciphertext);
        }
        catch (Exception e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
    }
    
    /** Decrypt base64 encoded and encrypted String */ 
    public byte[] decrypt(byte crypt[]) throws EncryptionException
    {
        if (crypt==null)
        {
            throw new NullPointerException("Byte array can't be null."); 
        }
        
        try
        {
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(crypt);
        }
        catch (Exception e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
    }
    
    /** Create new string using the configured character set */
    public String newString(byte[] bytes)
    {
        return new String(bytes,charSet); 
    }
  
}