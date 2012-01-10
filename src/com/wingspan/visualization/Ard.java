package com.wingspan.visualization;


@SuppressWarnings("serial")
public class Ard extends PApplet {

	private Serial myPort;
	private String str = "";
	private int lf = 10;

	public void setup() {
		size(1000, 1000, P3D);
		myPort = new Serial(this, Serial.list()[5], 115200);
		myPort.clear();
		background(0);
		fill(255, 69, 0);
		stroke(30, 15, 10);
		
		lightSpecular(1, 1, 1);
		directionalLight((float) 0.8, (float) 0.8, (float) 0.8, (float) 0.0, (float) 0.0, (float) -1.0);
	}

	// 480,591,207,425,306,366,432,568,201,448,753,387,531,639,675,604,304,448,307,655,515,287,671,486,433,547,220,536,608,232
	public void draw() {
		translate(150, 300, 0);
		// spotLight((float) 255, (float) 255, (float) 109, (float) 0, (float) 40, (float) 200, (float) 0, (float) -0.5, (float) -0.5, (float) PI / 2, (float) 2);
		lights();
		while (myPort.available() > 0) {
			background(0);

			str = myPort.readStringUntil(lf);
			if (str != null) {
				str = str.trim();
				println(str);
				String[] split = str.split(",");
				if (split.length == 30) {
					for (int i = 0; i < split.length; i = i + 3) {
						

						float x = normalizeX(Integer.parseInt(split[i]));
						float y = normalizeY(Integer.parseInt(split[i + 1]));
						float z = normalizeZ(Integer.parseInt(split[i + 2]));
//						if (i==0){
//							x=0;y=0;
//						}else if (i==3){
//							x=0;y=0;
//						}else if (i==6){
//							x=0;y=0;
//						}else if (i==9){
//							x=0;y=0;
//						}
						pushMatrix();
						rotateX(x);
						rotateY(y);
						rotateZ(z);
						box(50, 150, 80);
						popMatrix();
						if (i == 12){
							translate(0, 250, 0);
						} else if (i < 12) {
							translate(150, 0, 0);
						} else {
							translate(-150, 0, 0);
						}
					}
				}
			}
		}
	}
	
	public void stop(){
		myPort.stop();
		super.stop();
	} 

	public float normalizeX(int x) {
		return (float) (x - 230) * (float) (Math.PI / (728 - 230));
	}

	public float normalizeY(int y) {
		return (float) (y - 265) * (float) (Math.PI / (764 - 265));
	}

	public float normalizeZ(int z) {
		return (float) (z - 175) * (float) (Math.PI / (680 - 175));
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "--present", "basic.Ard" });
	}
}
