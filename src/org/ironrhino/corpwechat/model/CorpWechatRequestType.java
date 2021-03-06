package org.ironrhino.corpwechat.model;

import org.ironrhino.core.model.Displayable;

public enum CorpWechatRequestType implements Displayable {

	text, image, voice, video, location, link, event;

	public String getName() {
		return name();
	}

	public String getDisplayName() {
		return Displayable.super.getDisplayName();
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

}
