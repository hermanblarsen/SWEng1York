package com.i2lp.edi.server.packets;

import org.apache.xpath.ExpressionNode;

import java.util.ArrayList;

/**
 * Created by zain on 01/06/2017.
 */
public class PresentationStatisticsRecord {
	int presentationID;
	int userID;
	String slideTimesRaw;
	ArrayList<Integer> slideTimes = new ArrayList<>();

	public PresentationStatisticsRecord(int presentationID, int userID, String slideTimesRaw){
		this.presentationID = presentationID;
		this.userID = userID;
		this.slideTimesRaw = slideTimesRaw;
		processSlideTimes();
	}

	private void processSlideTimes(){
		// Format: Slide Number,Time spent on slide(Secs)\n
		//Split into rows.
		for(String row : slideTimesRaw.split("\n")){
			//Split into columns.
			int slide = Integer.parseInt(row.split(",")[0]);
			int time = Integer.parseInt(row.split(",")[1]);

			slideTimes.add(time);

		}
	}

	public int getPresentationID() {
		return presentationID;
	}

	public int getUserID() {
		return userID;
	}

	public String getSlideTimesRaw() {
		return slideTimesRaw;
	}

	public ArrayList<Integer> getSlideTimes() {
		return slideTimes;
	}
}
