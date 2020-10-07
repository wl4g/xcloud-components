package com.wl4g.components.common.nlp;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Chinese processing conversion recognition tools.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2017-03-29
 * @sine v1.0.0
 * @see
 */
public abstract class PingyUtils {

	/**
	 * Gets Chinese spelling
	 * 
	 * @param cnStrring
	 *            Target Chinese string
	 * @return
	 */
	public static List<String> getPingyin(String cnStrring) {
		List<String> pingys = null;
		try {
			char[] chArr = cnStrring.toCharArray();
			String[] strArr = new String[chArr.length];

			HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
			t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
			t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			t3.setVCharType(HanyuPinyinVCharType.WITH_V);

			pingys = new ArrayList<String>(chArr.length);
			for (int i = 0; i < chArr.length; i++) {
				// 判断是否为汉字字符
				if (java.lang.Character.toString(chArr[i]).matches("[\\u4E00-\\u9FA5]+")) {
					strArr = PinyinHelper.toHanyuPinyinStringArray(chArr[i], t3);
					pingys.add(strArr[0]);
				} else {
					pingys.add(Character.toString(chArr[i]));
				}
			}

			return pingys;
		} catch (BadHanyuPinyinOutputFormatCombination e1) {
			e1.printStackTrace();
		}

		return pingys;
	}

	/**
	 * Gets Chinese initials
	 * 
	 * @param cnStrring
	 *            Target Chinese string
	 * @return
	 */
	public static List<Character> getPinyinHeadChar(String cnStrring) {
		List<Character> pingys = new ArrayList<>(cnStrring.length());

		for (int j = 0; j < cnStrring.length(); j++) {
			char word = cnStrring.charAt(j);
			String[] pinyArr = PinyinHelper.toHanyuPinyinStringArray(word);
			if (pinyArr != null) {
				pingys.add(pinyArr[0].charAt(0));
			} else {
				pingys.add(word);
			}
		}

		return pingys;
	}

}
