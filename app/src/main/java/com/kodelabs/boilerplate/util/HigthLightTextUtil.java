package com.kodelabs.boilerplate.util;


public class HightLightTextUtil {
/**
     *  Regular expression pattern to match most part of RFC 3987
     *  Internationalized URLs, aka IRIs.  Commonly used Unicode characters are
     *  added.
     */
    public static final Pattern WEB_URL = Pattern.compile(
            "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
                    + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
                    + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
                    + "(?:" + DOMAIN_NAME + ")"
                    + "(?:\\:\\d{1,5})?)" // plus option port number
                    + "(\\/(?:(?:[" + GOOD_IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~"  // plus option query params
                    + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
                    + "(?:\\b|$)"); 
                    
 public static final Pattern MOBILE_PHONE
            = Pattern.compile(
            //"(^1\\d{10}$)|(^0\\d{10,11}$)|(^1\\d{2}-\\d{4}-\\d{4}$)|(^0\\d{2,3}-\\d{7,8}$)|(^\\d{7,8}$)|(^(4|8)00\\d{1}-\\d{3}-\\d{3}$)|(^(4|8)00\\d{7}$)");
            "(^|(?<=\\D))((1\\d{10})|(0\\d{10,11})|(1\\d{2}-\\d{4}-\\d{4})|(0\\d{2,3}-\\d{7,8})|(\\d{7,8})|((4|8)00\\d{1}-\\d{3}-\\d{3})|((4|8)00\\d{7}))(?!\\d)");

public static void makeHigthLightTaskText(TextView textView, SpannableString spannableString, HightLightTaskClickListener nameListner, String TASK_MATCHERS, HightLightTaskClickListener listener, int textColorTask, int textcolorLink, int textcolorName, boolean isMatchName) {
		if(textView == null || spannableString == null) {
			return;
		}
		final Context ctx = textView.getContext();
		String matcherStr = "";
		SpannableStringBuilder style = new SpannableStringBuilder(spannableString);
		boolean isFind = false;
		if(!TextUtils.isEmpty(TASK_MATCHERS)) {
			Pattern p = Pattern.compile(TASK_MATCHERS);
			Matcher matcher = p.matcher(spannableString);
			while (matcher.find()) {
				isFind = true;
				style.setSpan(new HightLightTaskClickSpan(matcher.group(), textView.getContext().getResources().getColor(textColorTask), listener, false),
						matcher.start(), matcher.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}


		Matcher matcherAtURL = WEB_URL.matcher(spannableString);
		while (matcherAtURL.find()) {
			matcherStr = matcherAtURL.group();
			if (matcherStr.toUpperCase().startsWith("HTTP://") || matcherStr.toUpperCase().startsWith("HTTPS://")) {
				isFind = true;
				style.setSpan(new HightLightTaskClickSpan(matcherAtURL.group(), textView.getContext().getResources().getColor(textcolorLink), new HightLightTaskClickListener(){
							@Override
							public void onClick(String text) {
								//todo
							}
						},true),
						matcherAtURL.start(), matcherAtURL.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}

		}

		Matcher matcherAtPhone = MOBILE_PHONE.matcher(spannableString);
		while (matcherAtPhone.find()) {
			final String myPhone = matcherAtPhone.group();
			isFind = true;
			style.setSpan(new HightLightTaskClickSpan(matcherAtPhone.group(), textView.getContext().getResources().getColor(textcolorLink), new HightLightTaskClickListener(){
						@Override
						public void onClick(String text) {
							//todo
						}
					},true),
					matcherAtPhone.start(), matcherAtPhone.end(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		}

		if(isFind) {
			textView.setMovementMethod(LinkMovementMethod.getInstance());
		}
		textView.setText(style);

	}
	
	}
