package com.wingspan.android.bluetooth;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

public class PostureClassifier {

	protected svm_model model = null;
	
	public PostureClassifier(){
		// load model
	}	
	
	public void train(){
		// check speed vs C
	}
	
	public double predict(svm_node[] x){
		// check x
		double d = svm.svm_predict(model, x);
		return d;
	}
	
	public void loadModel(String modelFilename) throws IOException{
		BufferedReader input = new BufferedReader(new FileReader(modelFilename));
		model = svm.svm_load_model(input);
		//System.out.println(model.rho[0]);
		input.close();
	}
	
	public svm_node[] createExample(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3,
			double z3, double x4, double y4, double z4, double x5, double y5, double z5,double x6, double y6, double z6,double x7, double y7,
			double z7,double x8, double y8, double z8,double x9, double y9, double z9,double x10, double y10, double z10){
			svm_node[] x = new svm_node[30];
			x[0]=createNode(1,x1);
			x[1]=createNode(2,y1);
			x[2]=createNode(3,z1);
			x[3]=createNode(4,x2);
			x[4]=createNode(5,y2);
			x[5]=createNode(6,z2);
			x[6]=createNode(7,x3);
			x[7]=createNode(8,y3);
			x[8]=createNode(9,z3);
			x[9]=createNode(10,x4);
			x[10]=createNode(11,y4);
			x[11]=createNode(12,z4);
			x[12]=createNode(13,x5);
			x[13]=createNode(14,y5);
			x[14]=createNode(15,z5);
			x[15]=createNode(16,x6);
			x[16]=createNode(17,y6);
			x[17]=createNode(18,z6);
			x[18]=createNode(19,x7);
			x[19]=createNode(20,y7);
			x[20]=createNode(21,z7);
			x[21]=createNode(22,x8);
			x[22]=createNode(23,y8);
			x[23]=createNode(24,z8);
			x[24]=createNode(25,x9);
			x[25]=createNode(26,y9);
			x[26]=createNode(27,z9);
			x[27]=createNode(28,x10);
			x[28]=createNode(29,y10);
			x[29]=createNode(30,z10);
			return x;
	}
	
	private svm_node createNode(int index, double value){
		svm_node node = new svm_node();
		node.index=index;
		node.value=value;
		return node;
	}
	
	public static void main(String[] args){
		String modelFilename = "/Users/tholloway/Desktop/libsvm-3.11/train.scale.model";
		PostureClassifier classifier = new PostureClassifier();
		try {
			classifier.loadModel(modelFilename);
			svm_node[] example = classifier.createExample(0.653763,0.331288,0.910686,0.329741,0.626838,0.93032,0.571429,0.705128,0.889483,0.0636364,0.263793,0.812379,0.37659,0.0649123,0.807615,0.272727,0.250386,0.78937,0.1875,0.360544,0.761364,0.284768,0.805808,0.736364,0.254762,0.785473,0.730673,0.75811,0.844488,0.486141); 
			example = classifier.createExample(0.64086,0.380368,0.940989,0.310345,0.676471,0.934087,0.583851,0.732372,0.877005,0.0431818,0.291379,0.825919,0.318066,0.045614,0.819639,0.198135,0.222566,0.779528,0.166667,0.311224,0.736742,0.245033,0.805808,0.747727,0.271429,0.805743,0.730673,0.769481,0.836614,0.477612); 

			double prediction = classifier.predict(example);
			System.out.println(prediction);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
