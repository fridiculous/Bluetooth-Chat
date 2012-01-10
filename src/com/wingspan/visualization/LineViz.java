package com.wingspan.visualization;

import processing.core.PApplet;

@SuppressWarnings("serial")
public class LineViz extends PApplet {

	Stripe[] stripes = new Stripe[50];

	public void setup() {
		size(200, 200);
		
		for (int i = 0; i < stripes.length; i++) {
			stripes[i] = new Stripe(this);
		}
	}

	public void draw() {
		background(100);
		for (int i = 0; i < stripes.length; i++) {
			stripes[i].move();
			stripes[i].display();
		}
		ellipse(50, 50, 80, 80);
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "--present", "basic.LineViz" });
	}
}
