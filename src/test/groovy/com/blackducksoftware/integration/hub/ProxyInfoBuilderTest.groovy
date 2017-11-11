package com.blackducksoftware.integration.hub

import org.apache.commons.lang3.StringUtils
import org.junit.Test

import com.blackducksoftware.integration.encryption.PasswordEncrypter
import com.blackducksoftware.integration.hub.proxy.ProxyInfo
import com.blackducksoftware.integration.hub.proxy.ProxyInfoBuilder

class ProxyInfoBuilderTest {

    @Test
    public void testBuilder() {
        final String username = "username"
        final String password = "password"
        final String proxyHost = "proxyHost"
        final int proxyPort = 25
        final String proxyIgnoredHosts = ".*"
        final ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort
        builder.username = username
        builder.password = password
        builder.ignoredProxyHosts = proxyIgnoredHosts
        final ProxyInfo proxyInfo1 = builder.build()
        final String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert proxyIgnoredHosts == proxyInfo1.ignoredProxyHosts

        assert maskedPassword.length() == password.length()
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")

        assert builder.hasProxySettings()
    }

    @Test
    public void testEncryptedPasswordBuilder() {
        final String username = "username"
        final String password = "password"
        final String proxyHost = "proxyHost"
        final int proxyPort = 25
        final String proxyIgnoredHosts = ".*"
        final String encryptedPassword = PasswordEncrypter.encrypt(password)
        final ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort
        builder.username = username
        builder.password = encryptedPassword
        builder.passwordLength = password.length()
        builder.ignoredProxyHosts = proxyIgnoredHosts
        final ProxyInfo proxyInfo1 = builder.build()
        final String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert proxyIgnoredHosts == proxyInfo1.ignoredProxyHosts

        assert password != builder.password
        assert password.length() == builder.passwordLength
        assert maskedPassword.length() == password.length()
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")

        assert builder.hasProxySettings()
    }

    @Test
    public void testUnauthenticatedBuilder() {
        final String proxyHost = "proxyHost"
        final int proxyPort = 25
        final String proxyIgnoredHosts = ".*"
        final ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort
        builder.ignoredProxyHosts = proxyIgnoredHosts
        final ProxyInfo proxyInfo1 = builder.build()
        final String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert proxyIgnoredHosts == proxyInfo1.ignoredProxyHosts
        assert builder.hasProxySettings()
    }
}
