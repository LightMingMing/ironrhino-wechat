package org.ironrhino.corpwechat.model;

import org.ironrhino.core.model.Displayable;
import org.ironrhino.core.struts.I18N;

public enum CorpWechatMediaType implements Displayable {

	image(1024 * 1024), voice(2 * 1024 * 1024), video(10 * 1024 * 1024), file(
			10 * 1024 * 1024);

	private int maxFileLength;

	private CorpWechatMediaType(int maxFileLength) {
		this.maxFileLength = maxFileLength;
	}

	public int getMaxFileLength() {
		return maxFileLength;
	}

	@Override
	public String getName() {
		return this.name();
	}

	@Override
	public String getDisplayName() {
		try {
			return I18N.getText(getClass(), name());
		} catch (Exception e) {
			return name();
		}
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

}
