package org.ironrhino.wechat.handler;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.wechat.model.WechatRequest;
import org.ironrhino.wechat.model.WechatRequestType;
import org.ironrhino.wechat.model.WechatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(-1)
@Component
public class EventWechatRequestHandler implements WechatRequestHandler {

	@Autowired(required = false)
	private List<ScanEventHandler> scanEventHandlers;

	@Autowired(required = false)
	private ScanStringEventHandler scanStringEventHandler;

	@Autowired(required = false)
	private List<ClickEventHandler> clickEventHandlers;

	@Autowired(required = false)
	private List<LocationEventHandler> locationEventHandlers;

	@Autowired(required = false)
	private List<ScancodeEventHandler> scancodeEventHandlers;

	@Autowired(required = false)
	private List<PhotoEventHandler> photoEventHandlers;

	@Autowired(required = false)
	private List<UnsubscribeEventHandler> unsubscribeEventHandlers;

	@Override
	public WechatResponse handle(WechatRequest request) {
		if (request.getMsgType() != WechatRequestType.event || request.getEvent() == null)
			return null;
		String eventKey = request.getEventKey();
		switch (request.getEvent()) {
		case SCAN:
		case subscribe:
			int key = 0;
			if (StringUtils.isNotBlank(eventKey)) {
				if (eventKey.startsWith("qrscene_"))
					eventKey = eventKey.substring(eventKey.indexOf('_') + 1);
				if (StringUtils.isNumeric(eventKey)
						&& eventKey.length() <= String.valueOf(Integer.MAX_VALUE).length()) {
					key = Integer.valueOf(eventKey);
				} else if (scanStringEventHandler != null) {
					WechatResponse wr = scanStringEventHandler.handle(eventKey, request);
					return wr != null ? wr : WechatResponse.EMPTY;
				}
			}
			if (scanEventHandlers != null)
				for (ScanEventHandler seh : scanEventHandlers)
					if (seh.takeover(key)) {
						WechatResponse wr = seh.handle(key, request);
						return wr != null ? wr : WechatResponse.EMPTY;
					}
			break;
		case CLICK:
			if (clickEventHandlers != null)
				for (ClickEventHandler ceh : clickEventHandlers)
					if (ceh.takeover(eventKey)) {
						WechatResponse wr = ceh.handle(eventKey, request);
						return wr != null ? wr : WechatResponse.EMPTY;
					}

			break;
		case LOCATION:
		case location_select:
			if (locationEventHandlers != null)
				for (LocationEventHandler leh : locationEventHandlers)
					if (leh.takeover(eventKey)) {
						WechatResponse wr = leh.handle(request.getLatitude(), request.getLongitude(), request);
						return wr != null ? wr : WechatResponse.EMPTY;
					}

			break;
		case scancode_push:
		case scancode_waitmsg:
			if (scancodeEventHandlers != null)
				for (ScancodeEventHandler seh : scancodeEventHandlers)
					if (seh.takeover(eventKey)) {
						WechatResponse wr = seh.handle(request.getScanResult(), request);
						return wr != null ? wr : WechatResponse.EMPTY;
					}
			break;
		case pic_photo_or_album:
		case pic_sysphoto:
			if (photoEventHandlers != null)
				for (PhotoEventHandler peh : photoEventHandlers)
					if (peh.takeover(eventKey)) {
						WechatResponse wr = peh.handle(request);
						return wr != null ? wr : WechatResponse.EMPTY;
					}
			break;

		case unsubscribe:
			if (unsubscribeEventHandlers != null)
				for (UnsubscribeEventHandler ueh : unsubscribeEventHandlers) {
					WechatResponse wr = ueh.handle(request);
					if (wr != null)
						return wr;
				}
		default:
			break;
		}
		return null;
	}

}
