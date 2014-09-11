package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Utils {
	
	
	public static final Pattern HASHTAGS_PATTERN = Pattern.compile("(#[a-zабвгдѓежзѕијклљмнњопрстќуфхцчџш0-9]++)");
	public static DateFormat df = new SimpleDateFormat("kk:mm:ss dd.MM.yyyy");
	public static DateFormat timestamp_df = new SimpleDateFormat("'time'__k_mm_ss__'date'_dd_MM_yyyy");
	
	
	public static List<String> findAll(String text, Pattern pattern) {
		List<String> res = new LinkedList<String>();
		Matcher m = pattern.matcher(text);
		while ( m.find() ) {
			res.add(m.group());
		}
		return res;
	}

}
