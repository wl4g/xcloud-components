package com.wl4g.component.rpc.codec.fieldseq;

public class FieldMsg {
	static String[] fieldSequence = new String[] { "id", "version", "command" };
	String command;
	int id;
	byte version;
}
