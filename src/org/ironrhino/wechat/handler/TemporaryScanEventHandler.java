package org.ironrhino.wechat.handler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.wechat.model.WechatRequest;
import org.ironrhino.wechat.model.WechatResponse;
import org.ironrhino.wechat.service.Wechat;
import org.ironrhino.wechat.support.WechatQRCodeControl;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Getter;
import lombok.Setter;

public abstract class TemporaryScanEventHandler extends ScanEventHandler {

	public static final int DEFAULT_EXPIRE_IN_SECONDS = 600;

	private static final String DEFAULT_CACHE_NAMESPACE = "wechat:";

	@Autowired
	protected CacheManager cacheManager;

	@Autowired
	protected Wechat wechat;

	@Autowired
	protected WechatQRCodeControl wechatQRCodeControl;

	@Getter
	@Setter
	protected String cacheNamespace = DEFAULT_CACHE_NAMESPACE;

	@Getter
	@Setter
	protected int expiresInSeconds = DEFAULT_EXPIRE_IN_SECONDS;

	@Setter
	protected String cacheKeyPrefix;

	public String getCacheKeyPrefix() {
		if (cacheKeyPrefix == null) {
			String str = getClass().getSimpleName();
			if (str.endsWith("ScanEventHandler"))
				str = str.substring(0, str.length() - "ScanEventHandler".length());
			str = str.toLowerCase() + ":";
			cacheKeyPrefix = str;
		}
		return cacheKeyPrefix;
	}

	protected int generateEventKey(String businessKey) {
		Integer eventKey = randomKey();
		while (cacheManager.containsKey(getCacheKeyPrefix() + eventKey, getCacheNamespace()))
			eventKey = randomKey();
		cacheManager.put(getCacheKeyPrefix() + eventKey, businessKey, getExpiresInSeconds(), TimeUnit.SECONDS,
				getCacheNamespace());
		return eventKey;
	}

	public String createQRCode(String businessKey) throws IOException {
		String qrcode = (String) cacheManager.get(getCacheKeyPrefix() + businessKey, getCacheNamespace());
		if (qrcode != null)
			return qrcode;
		int eventKey = generateEventKey(businessKey);
		qrcode = wechatQRCodeControl.createTemporary(eventKey, getExpiresInSeconds());
		cacheManager.put(getCacheKeyPrefix() + businessKey, qrcode, getExpiresInSeconds(), TimeUnit.SECONDS,
				getCacheNamespace());
		return qrcode;
	}

	@Override
	public WechatResponse handle(int eventKey, WechatRequest request) {
		String businessKey = (String) cacheManager.get(getCacheKeyPrefix() + eventKey, getCacheNamespace());
		cacheManager.delete(getCacheKeyPrefix() + eventKey, getCacheNamespace());
		cacheManager.delete(getCacheKeyPrefix() + businessKey, getCacheNamespace());
		return doHandle(businessKey, eventKey, request);
	}

	public abstract WechatResponse doHandle(String businessKey, int eventKey, WechatRequest request);

}
