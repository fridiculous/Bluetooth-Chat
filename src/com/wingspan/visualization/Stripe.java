package com.wingspan.visualization;

import processing.core.PApplet;

public class Stripe {
	protected PApplet parent; // The parent PApplet that we will render ourselves onto
	protected float x; // horizontal location of stripe
	protected float speed; // speed of stripe
	protected float w; // width of stripe
	protected boolean mouse; // state of stripe (mouse is over or not?)

	public Stripe(PApplet p) {
		parent = p;
		x = 0; 
		speed = parent.random(1);
		w = parent.random(10, 30);
		mouse = false;
	}

	public void display() {
		parent.fill(255, 100);
		parent.noStroke();
		parent.rect(x, 0, w, parent.height);
	}

	public void move() {
		x += speed;
		if (x > parent.width + 20)
			x = -20;
	}
}