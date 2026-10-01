package dev.a2ahub.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AesGcmAttributeConverter Encryption Unit Tests")
class AesGcmAttributeConverterTest {

    private AesGcmAttributeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new AesGcmAttributeConverter();
        SecurityProperties properties = new SecurityProperties();
        properties.setEncryptionKey("test-secret-encryption-key-32-chars-long!");
        converter.configure(properties);
    }

    @Test
    @DisplayName("Should encrypt and decrypt token symmetrically with AES-GCM-256")
    void shouldEncryptAndDecryptSuccessfully() {
        String sensitiveToken = "bearer_secret_token_live_xyz_987654";

        String dbCipher = converter.convertToDatabaseColumn(sensitiveToken);

        assertThat(dbCipher).isNotNull();
        assertThat(dbCipher).startsWith("ENC:");
        assertThat(dbCipher).isNotEqualTo(sensitiveToken);

        String decrypted = converter.convertToEntityAttribute(dbCipher);
        assertThat(decrypted).isEqualTo(sensitiveToken);
    }

    @Test
    @DisplayName("Should return null when converting null attributes")
    void shouldHandleNullGracefully() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("Should support backward compatibility for unencrypted legacy tokens")
    void shouldSupportLegacyPlaintext() {
        String plaintext = "legacy_token_123";
        String decrypted = converter.convertToEntityAttribute(plaintext);
        assertThat(decrypted).isEqualTo(plaintext);
    }
}
