package com.blackducksoftware.integration.hub

import org.junit.Test

import com.blackducksoftware.integration.hub.proxy.ProxyInfoFieldEnum
import com.blackducksoftware.integration.hub.validator.ProxyInfoValidator
import com.blackducksoftware.integration.validator.ValidationResults

class ProxyInfoValidatorTest {

    @Test
    public void testProxyValid() {
        ProxyInfoValidator validator = new ProxyInfoValidator()
        ValidationResults result = validator.assertValid()
        assert !validator.hasProxySettings()
        assert result.success

        result = new ValidationResults()
        String proxyHost = "proxyhost"
        int proxyPort = 25
        validator.host = proxyHost
        validator.port = proxyPort
        assert validator.hasProxySettings()

        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPort = 25
        String username = "proxyUser"
        String password = "proxyPassword"
        String ignoredHost = ".*"
        validator.host = proxyHost
        validator.port = proxyPort
        validator.username = username
        validator.password = password
        validator.ignoredProxyHosts = ignoredHost
        result = validator.assertValid()
        assert proxyHost == validator.host
        assert proxyPort == validator.port
        assert username == validator.username
        assert password == validator.password
        assert ignoredHost == validator.ignoredProxyHosts
        assert validator.hasProxySettings()
        assert result.success
    }

    @Test
    public void testInvalidPort() {
        ProxyInfoValidator validator = new ProxyInfoValidator()
        ValidationResults result = new ValidationResults()
        String proxyHost = "proxyhost"
        int proxyPort = -1
        validator.host = proxyHost
        validator.port = proxyPort
        validator.validatePort(result)
        assert proxyPort == Integer.parseInt(validator.port)
        assert "-1" == validator.port
        assert validator.hasProxySettings()
        assert result.hasErrors()
        assert result.getResultString(ProxyInfoFieldEnum.PROXYPORT).contains(ProxyInfoValidator.MSG_PROXY_PORT_INVALID)

        validator = new ProxyInfoValidator()
        result = new ValidationResults()
        proxyHost = "proxyhost"
        String proxyPortStr = "dadfasf"
        validator.host = proxyHost
        validator.port = proxyPortStr
        validator.validatePort(result)
        assert proxyPortStr == validator.port
        assert validator.hasProxySettings()
        assert result.hasErrors()
        assert null != result.getResultString(ProxyInfoFieldEnum.PROXYPORT)

        validator = new ProxyInfoValidator()
        result = new ValidationResults()
        proxyHost = ""
        proxyPortStr = "25"
        validator.host = proxyHost
        validator.port = proxyPortStr
        validator.validatePort(result)
        assert proxyPortStr == validator.port
        assert validator.hasProxySettings()
        assert result.hasErrors()
        assert result.getResultString(ProxyInfoFieldEnum.PROXYHOST).contains(ProxyInfoValidator.MSG_PROXY_HOST_REQUIRED)

        validator = new ProxyInfoValidator()
        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPortStr = ""
        validator.host = proxyHost
        validator.port = proxyPortStr
        validator.validatePort(result)
        assert proxyPortStr == validator.port
        assert validator.hasProxySettings()
        assert result.hasErrors()
        assert result.getResultString(ProxyInfoFieldEnum.PROXYPORT).contains(ProxyInfoValidator.MSG_PROXY_PORT_REQUIRED)
    }

    @Test
    public void testValidCredentials() {
        ProxyInfoValidator validator = new ProxyInfoValidator()
        ValidationResults result = new ValidationResults()
        String proxyHost = "proxyhost"
        String proxyPort = 25
        String username = "proxyUser"
        String password = "proxyPassword"
        validator.host = proxyHost
        validator.port = proxyPort
        validator.username = username
        validator.password = password
        assert validator.hasAuthenticatedProxySettings()
        assert validator.hasProxySettings()
        assert result.success
    }

    @Test
    public void testInvalidCredentials() {

        ProxyInfoValidator validator = new ProxyInfoValidator()
        ValidationResults result = new ValidationResults()
        String proxyHost = ""
        String proxyPort = 25
        String username = "proxyUser"
        String password = "proxyPassword"
        String ignoredHost = ".*"
        validator.host = proxyHost
        validator.port = proxyPort
        validator.username = username
        validator.password = password
        validator.validateCredentials(result)
        assert proxyHost == validator.host
        assert proxyPort == validator.port
        assert username == validator.username
        assert password == validator.password
        assert validator.hasProxySettings()
        assert result.hasErrors()
        assert result.getResultString(ProxyInfoFieldEnum.PROXYHOST).contains(ProxyInfoValidator.MSG_PROXY_HOST_NOT_SPECIFIED)

        validator = new ProxyInfoValidator()
        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPort = 25
        username = "proxyUser"
        password = null
        ignoredHost = ".*"
        validator.host = proxyHost
        validator.port = proxyPort
        validator.username = username
        validator.password = password
        validator.validateCredentials(result)
        assert proxyHost == validator.host
        assert proxyPort == validator.port
        assert username == validator.username
        assert password == validator.password
        assert !validator.hasAuthenticatedProxySettings()
        assert validator.hasProxySettings()
        assert result.hasErrors()
        assert result.getResultString(ProxyInfoFieldEnum.PROXYUSERNAME).contains(ProxyInfoValidator.MSG_CREDENTIALS_INVALID)
        assert result.getResultString(ProxyInfoFieldEnum.PROXYPASSWORD).contains(ProxyInfoValidator.MSG_CREDENTIALS_INVALID)

        validator = new ProxyInfoValidator()
        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPort = 25
        username = null
        password = "proxyPassword"
        validator.host = proxyHost
        validator.port = proxyPort
        validator.username = username
        validator.password = password
        validator.validateCredentials(result)
        assert proxyHost == validator.host
        assert proxyPort == validator.port
        assert username == validator.username
        assert password == validator.password
        assert !validator.hasAuthenticatedProxySettings()
        assert validator.hasProxySettings()
        assert result.hasErrors()
        assert result.getResultString(ProxyInfoFieldEnum.PROXYUSERNAME).contains(ProxyInfoValidator.MSG_CREDENTIALS_INVALID)
        assert result.getResultString(ProxyInfoFieldEnum.PROXYPASSWORD).contains(ProxyInfoValidator.MSG_CREDENTIALS_INVALID)
    }

    @Test
    public void testIgnoredHostValid() {
        ProxyInfoValidator validator = new ProxyInfoValidator()
        ValidationResults result = new ValidationResults()
        String proxyHost = "proxyhost"
        String proxyPort = 25
        String ignoredHost = ".*,.*"
        validator.host = proxyHost
        validator.port = proxyPort
        validator.ignoredProxyHosts = ignoredHost
        validator.validateIgnoreHosts(result)
        assert ignoredHost == validator.ignoredProxyHosts
        assert validator.hasProxySettings()
        assert result.success
    }

    @Test
    public void testIgnoredHostInvalid() {
        ProxyInfoValidator validator = new ProxyInfoValidator()
        ValidationResults result = new ValidationResults()
        String proxyHost = "proxyhost"
        String proxyPort = 25
        String username = "proxyUser"
        String password = "proxyPassword"
        String ignoredHost = ".asdfajdflkjaf{ ])(faslkfj,{][[)("
        validator.host = proxyHost
        validator.port = proxyPort
        validator.username = username
        validator.password = password
        validator.ignoredProxyHosts = ignoredHost
        validator.validateIgnoreHosts(result)
        assert ignoredHost == validator.ignoredProxyHosts
        assert validator.hasProxySettings()
        assert result.hasErrors()
        assert result.getResultString(ProxyInfoFieldEnum.NOPROXYHOSTS).contains(ProxyInfoValidator.MSG_IGNORE_HOSTS_INVALID)

        validator = new ProxyInfoValidator()
        result = new ValidationResults()
        proxyHost = ""
        ignoredHost = ".asdfajdflkjaf{ ])(faslkfj"
        validator.host = proxyHost
        validator.port = proxyPort
        validator.ignoredProxyHosts = ignoredHost
        validator.validateIgnoreHosts(result)
        assert ignoredHost == validator.ignoredProxyHosts
        assert validator.hasProxySettings()
        assert result.hasErrors()
        assert result.getResultString(ProxyInfoFieldEnum.PROXYHOST).contains(ProxyInfoValidator.MSG_PROXY_HOST_NOT_SPECIFIED)
    }
}
