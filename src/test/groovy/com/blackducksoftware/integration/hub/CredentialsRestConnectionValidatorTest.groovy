package com.blackducksoftware.integration.hub

import org.apache.commons.lang3.StringUtils
import org.junit.Test

import com.blackducksoftware.integration.hub.rest.RestConnectionFieldEnum
import com.blackducksoftware.integration.hub.validator.CredentialsRestConnectionValidator
import com.blackducksoftware.integration.log.IntLogger
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger
import com.blackducksoftware.integration.validator.ValidationResults

class CredentialsRestConnectionValidatorTest {

    @Test
    public void testValidCredentials() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        String username = "username"
        String password = "password"
        validator.username = username
        validator.password = password
        ValidationResults result = new ValidationResults()
        validator.validateAdditionalFields(result)
        assert username == validator.username
        assert password == validator.password
        assert result.success
    }

    @Test
    public void testInvalidCredentials() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        String username = ""
        String password = "password"
        validator.username = username
        validator.password = password
        ValidationResults result = new ValidationResults()
        validator.validateAdditionalFields(result)
        assert username == validator.username
        assert password == validator.password
        assert result.hasErrors()

        validator = new CredentialsRestConnectionValidator()
        username = "username"
        password = ""
        validator.username = username
        validator.password = password
        result = new ValidationResults()
        validator.validateAdditionalFields(result)
        assert username == validator.username
        assert password == validator.password
        assert result.hasErrors()

        validator = new CredentialsRestConnectionValidator()
        username = ""
        password = ""
        validator.username = username
        validator.password = password
        result = new ValidationResults()
        validator.validateAdditionalFields(result)
        assert username == validator.username
        assert password == validator.password
        assert result.hasErrors()
    }

    @Test
    public void testValid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        validator.baseUrl = "http://www.google.com"
        validator.username = "username"
        validator.password = "password"
        validator.timeout = 120
        validator.logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)
        validator.commonRequestHeaders = new HashMap<>()
        assert validator.assertValid().success
    }

    @Test
    public void testInvalid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        validator.baseUrl = null
        validator.username = null
        validator.password = null
        validator.setTimeout(-1)
        validator.setLogger(null)
        validator.setCommonRequestHeaders(null)
        ValidationResults result = validator.assertValid()
        assert !result.success
    }

    @Test
    public void testBaseUrlValid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        String baseUrl = "http://www.google.com"
        validator.setBaseUrl(baseUrl)
        ValidationResults result = new ValidationResults()
        validator.validateBaseUrl(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.URL)
        assert baseUrl == validator.getBaseUrl()
        assert result.success
        assert StringUtils.isBlank(resultString)
    }

    @Test
    public void testBaseUrlInvalid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        String baseUrl = null
        validator.setBaseUrl(baseUrl)
        ValidationResults result = new ValidationResults()
        validator.validateBaseUrl(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.URL)
        assert null == validator.getBaseUrl()
        assert result.hasErrors()
        assert StringUtils.isNotBlank(resultString)
        assert resultString.contains(CredentialsRestConnectionValidator.ERROR_MSG_URL_NOT_FOUND)

        baseUrl = "htp:/a.bad.domain"
        validator.setBaseUrl(baseUrl)
        result = new ValidationResults()
        validator.validateBaseUrl(result)
        resultString = result.getResultString(RestConnectionFieldEnum.URL)
        assert baseUrl == validator.getBaseUrl()
        assert result.hasErrors()
        assert StringUtils.isNotBlank(resultString)
        assert resultString.contains(CredentialsRestConnectionValidator.ERROR_MSG_URL_NOT_VALID)
    }

    @Test
    public void testLoggerValid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)
        validator.setLogger(logger)
        ValidationResults result = new ValidationResults()
        validator.validateLogger(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.LOGGER)
        assert logger == validator.getLogger()
        assert result.success
        assert StringUtils.isBlank(resultString)
    }

    @Test
    public void testLoggerInvalid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        validator.setLogger(null)
        ValidationResults result = new ValidationResults()
        validator.validateLogger(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.LOGGER)
        assert null == validator.getLogger()
        assert result.hasErrors()
        assert StringUtils.isNotBlank(resultString)
        assert resultString.contains(CredentialsRestConnectionValidator.ERROR_MSG_LOGGER_NOT_VALID)
    }

    @Test
    public void testTimeoutValid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        int timeout = 120
        validator.setTimeout(timeout)
        ValidationResults result = new ValidationResults()
        validator.validateTimeout(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.TIMEOUT)
        assert timeout == validator.getTimeout()
        assert result.success
        assert StringUtils.isBlank(resultString)
    }

    @Test
    public void testTimeoutInvalid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        validator.setTimeout(-1)
        ValidationResults result = new ValidationResults()
        validator.validateTimeout(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.TIMEOUT)
        assert -1 == validator.getTimeout()
        assert result.hasErrors()
        assert StringUtils.isNotBlank(resultString)
        assert resultString.contains(CredentialsRestConnectionValidator.ERROR_MSG_TIMEOUT_NOT_VALID)
    }

    @Test
    public void testHeadersValid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        Map<String,String> headers = new HashMap<>();
        validator.setCommonRequestHeaders(headers)
        ValidationResults result = new ValidationResults()
        validator.validateCommonRequestHeaders(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.COMMON_HEADERS)
        assert headers == validator.getCommonRequestHeaders()
        assert result.success
        assert StringUtils.isBlank(resultString)
    }

    @Test
    public void testHeadersInvalid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        validator.setCommonRequestHeaders(null)
        ValidationResults result = new ValidationResults()
        validator.validateCommonRequestHeaders(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.COMMON_HEADERS)
        assert null == validator.getCommonRequestHeaders()
        assert result.hasErrors()
        assert StringUtils.isNotBlank(resultString)
        assert resultString.contains(CredentialsRestConnectionValidator.ERROR_MSG_COMMON_HEADERS_NOT_VALID)
    }

    @Test
    public void testProxyValid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        ValidationResults result = new ValidationResults()
        validator.validateProxyInfo(result)
        assert result.success

        result = new ValidationResults()
        String proxyHost = "proxyhost"
        int proxyPort = 25
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.validateProxyInfo(result)
        assert result.success

        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPort = 25
        String username = "proxyUser"
        String password = "proxyPassword"
        String ignoredHost = ".*"
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.proxyUsername = username
        validator.proxyPassword = password
        validator.proxyIgnoreHosts = ignoredHost
        validator.validateProxyInfo(result)
        assert proxyHost == validator.proxyHost
        assert proxyPort == validator.proxyPort
        assert username == validator.proxyUsername
        assert password == validator.proxyPassword
        assert ignoredHost == validator.proxyIgnoreHosts
        assert result.success
    }

    @Test
    public void testProxyInvalid() {
        CredentialsRestConnectionValidator validator = new CredentialsRestConnectionValidator()
        ValidationResults result = new ValidationResults()
        String proxyHost = "proxyhost"
        int proxyPort = -1
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.validateProxyInfo(result)
        assert result.hasErrors()

        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPort = 25
        String username = "proxyUser"
        String password = null
        String ignoredHost = ".*"
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.proxyUsername = username
        validator.proxyPassword = password
        validator.proxyIgnoreHosts = ignoredHost
        validator.validateProxyInfo(result)
        assert result.hasErrors()

        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPort = 25
        username = null
        password = "proxyPassword"
        ignoredHost = ".*"
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.proxyUsername = username
        validator.proxyPassword = password
        validator.proxyIgnoreHosts = ignoredHost
        validator.validateProxyInfo(result)
        assert result.hasErrors()

        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPort = 25
        username = "proxyUser"
        password = "proxyPassword"
        ignoredHost = ".asdfajdflkjaf{ ])(faslkfj"
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.proxyUsername = username
        validator.proxyPassword = password
        validator.proxyIgnoreHosts = ignoredHost
        validator.validateProxyInfo(result)
        assert result.hasErrors()
    }
}
