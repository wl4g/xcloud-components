package com.wl4g.component.rpc.codec.codectype;

import com.wl4g.component.rpc.codec.ObjectCoder;
import com.wl4g.component.rpc.codec.CodecConfig.EndianType;
import com.wl4g.component.rpc.codec.CodecConfig.TotalLengthType;
import com.wl4g.component.rpc.codec.helper.ByteHelper;
import com.wl4g.component.rpc.codec.type.OCInt16;
import com.wl4g.component.rpc.codec.wrap.IdentifyWrapper;

public class CodecTypeCoder {

	public static void main(String[] args) throws Exception {
		encodeMultiCmd();
	}

	private static void encodeMultiCmd() throws Exception {
		ObjectCoder coder = new ObjectCoder();
		coder.getCodecConfig().setEndianType(EndianType.LITTLE);
		coder.getCodecConfig().setTotalLengthType(TotalLengthType.HEAD_BODY);
		coder.getCodecConfig().setAutoLength(false);
		coder.getCodecConfig().addWrap(new IdentifyWrapper(new OCInt16(0xFAFB)));
		MultiCmdMsg msg = new MultiCmdMsg();
		msg.id = 32;
		msg.version = 1;
		msg.command1.setValue("ready");
		msg.command2.setValue("running");
		byte[] bytes = coder.encode(msg);
		System.out.println(ByteHelper.toHexString(bytes));
	}
}
