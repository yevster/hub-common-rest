package com.blackducksoftware.integration.hub.rest;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.HttpUrl;
import okhttp3.JavaNetCookieJar;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Request.Builder;

public class TokenRestConnection extends RestConnection {

	private static final String X_CSRF_TOKEN = "X-CSRF-TOKEN";
	private volatile long expiration = 0;
	private volatile String bearerToken = "";
	private final String apiKey;
	private final JsonParser parser = new JsonParser();
	private volatile String csrfToken;

	public TokenRestConnection(final IntLogger logger, final URL hubBaseUrl, String apiKey, final int timeout,
			final ProxyInfo proxyInfo) {
		super(logger, hubBaseUrl, timeout, proxyInfo);
		this.apiKey = apiKey;
	}

	@Override
	public void addBuilderAuthentication() throws IntegrationException {
		final CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		builder.cookieJar(new JavaNetCookieJar(cookieManager));
	}

	@Override
	public synchronized void clientAuthenticate() throws IntegrationException {
		final ArrayList<String> segments = new ArrayList<>();
		segments.addAll(Arrays.asList("token", "authenticate"));
		final HttpUrl httpUrl = createHttpUrl(segments, null);
		Map<String, String> headers = new HashMap<>(1);
		headers.put("Authorization", "token " + apiKey);

		final Request request = createPostRequest(httpUrl, headers, createJsonRequestBody(""));

		Response response = null;
		try {
			logRequestHeaders(request);
			response = getClient().newCall(request).execute();
			logResponseHeaders(response);
			if (!response.isSuccessful()) {
				throw new IntegrationRestException(response.code(), response.message(),
						String.format("Connection Error: %s %s", response.code(), response.message()));
			} else {
				// get the CSRF token
				this.csrfToken = response.header(X_CSRF_TOKEN);
				// Get the bearer token and expiration
				JsonElement parsedJson = parser.parse(response.body().charStream());
				JsonObject obj = parsedJson.getAsJsonObject();
				long expiresInMillis = obj.get("expiresInMilliseconds").getAsLong();
				// Expire slightly before the server - better to renew too soon than too late.
				this.expiration = System.currentTimeMillis() + expiresInMillis - 1000;
				this.bearerToken = obj.get("bearerToken").getAsString();
			}
		} catch (final IOException e) {
			throw new IntegrationException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(response);
		}
	}

	@Override
	protected Builder getRequestBuilder(Map<String, String> headers) {
		Builder builder = super.getRequestBuilder(headers);
		synchronized (this) {
			if (expiration <= System.currentTimeMillis()) {
				try {
					this.clientAuthenticate();
				} catch (Throwable t) {
					throw new RuntimeException("Authentication failure", t);
				}
			}
		}
		builder.addHeader("Authorization", "Bearer " + this.bearerToken);
		builder.addHeader(X_CSRF_TOKEN, this.csrfToken);
		return builder;
	}

}
