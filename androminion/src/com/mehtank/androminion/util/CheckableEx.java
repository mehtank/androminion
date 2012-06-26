package com.mehtank.androminion.util;

import android.widget.Checkable;

public interface CheckableEx extends Checkable {
	public void setChecked(boolean arg0, String indicator);
	public void setChecked(boolean arg0, int order, String indicator);
}
