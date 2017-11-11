package com.blackducksoftware.integration.hub

import org.apache.commons.lang3.StringUtils
import org.junit.Test

import com.blackducksoftware.integration.encryption.PasswordEncrypter

class CredentialsBuilderTest {

    @Test
    public void testBuildObject() {
        final String username = "username"
        final String password = "password"
        CredentialsBuilder builder = new CredentialsBuilder()
        builder.username = username
        builder.password = password
        Credentials credentials = builder.build()
        final String maskedPassword = credentials.getMaskedPassword()
        assert username == credentials.username
        assert password == credentials.decryptedPassword
        assert password != credentials.encryptedPassword
        assert password.length() == credentials.actualPasswordLength
        assert maskedPassword.length() == password.length()
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")
    }

    @Test
    public void testEncryptedPassword() {
        final String username = "username"
        final String password = "password"
        final String encryptedPassword = PasswordEncrypter.encrypt(password);
        CredentialsBuilder builder = new CredentialsBuilder()
        builder.username = username
        builder.password = encryptedPassword
        builder.passwordLength = password.length()
        Credentials credentials = builder.build()
        final String maskedPassword = credentials.getMaskedPassword()
        assert password.length() == builder.passwordLength
        assert username == credentials.username
        assert password == credentials.decryptedPassword
        assert password != credentials.encryptedPassword
        assert password.length() == credentials.actualPasswordLength
        assert maskedPassword.length() == password.length()
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")
    }
}
