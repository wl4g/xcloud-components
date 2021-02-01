package com.wl4g.component.rpc.codec.fieldseq;

import com.wl4g.component.rpc.codec.type.OCObject;
import com.wl4g.component.rpc.codec.type.OCString;

public class FieldCodecMsg extends OCObject {
	public FieldCodecMsg() {
		setFieldSequence(new String[] { "id", "version", "command" });
	}

	OCString command = new OCString();
	int id;
	byte version;
}
