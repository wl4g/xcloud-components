package com.wl4g.component.rpc.codec.codectype;

import com.wl4g.component.rpc.codec.type.OCInt32;
import com.wl4g.component.rpc.codec.type.OCString;

public class MultiCmdMsg {
	int id;
	byte version;
	OCInt32 cmdLen1 = new OCInt32();
	OCString command1 = new OCString(cmdLen1);
	OCInt32 cmdLen2 = new OCInt32();
	OCString command2 = new OCString(cmdLen2);
}
