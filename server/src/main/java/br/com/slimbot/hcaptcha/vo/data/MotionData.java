package br.com.slimbot.hcaptcha.vo.data;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MotionData {

	private int v = 1;
	private List<String> widgetList = new ArrayList<>();
	private String widgetId;
	private String href;
	private PrevData prev;
}
