package Problem;

import java.io.File;
import java.util.Arrays;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;

public class PadLocalizer2 {
	public static int numOfCols = 0;
	public static int numOfRows = 0;

	public static double[] horizon=null;
	
	public final static double[] EDGE_PIXELS_2 = {0, 0, 255};

	public static void main(String[] args) {
		localizePadInDir(EdgeDetection.IMAGE_SOURCE_DIR+"original\\", EdgeDetection.IMAGE_SOURCE_DIR+"bw\\", "out\\");
	}

	public static void localizePad(String originalImageFile, String grassImageFile, String outputImageFile) {
		Mat originalImage = Highgui.imread(originalImageFile);
		
		EdgeDetection.markFHWTEdgeHillTopsInRowsandCols(grassImageFile, outputImageFile);
		int columnOffset = numOfCols/5;
		
		double mean = horizon[horizon.length/3];
		Point lt = new Point(0, horizon[0]);
		Point lb = new Point(0, horizon[0]);
		Point rt = new Point(0, mean);
		Point rb = new Point(0, mean);
		boolean done = false, tdone = false;
		for(int i = 0; i < horizon.length; i++) {
			if(i < horizon.length/2) {
				if(horizon[i] >= lt.y && !tdone) {
					lt.x = columnOffset + i;
					lt.y = horizon[i];
				}
				if(horizon[i] < lb.y && !tdone) {
					if(Math.abs(horizon[i]-lb.y) > 2*Math.abs(horizon[i-1]-lb.y) && Math.abs(horizon[i]-lb.y) > 10) {
						rt.x = columnOffset + i+2;
						rt.y = horizon[i+2];
						tdone = true;
					}
					else {
						lb.x = columnOffset + i;
						lb.y = horizon[i];
					}
				}
			}
			else {
				if(horizon[i] >= rt.y && !done) {
					if(Math.abs(horizon[i]-rt.y) > 2*Math.abs(horizon[i-1]-rt.y) && Math.abs(horizon[i]-rt.y) > 20) {
						rt.x = columnOffset + i+2;
						rt.y = horizon[i+2];
						done = true;
					}
					else {
						rt.x = columnOffset + i;
						rt.y = horizon[i];
					}
				}
				if(horizon[i] <= rb.y && !done) {
					rb.x = columnOffset + i;
					rb.y = horizon[i];
				}
			}
		}
		double mmean1 = 0;
		for(int i = (int)(lt.x-columnOffset); i < (lt.x-columnOffset)+((rt.x-lt.x)/4); i++) {
			mmean1 += horizon[i];
		}
		mmean1 /= ((rt.x-lt.x)/4);

		double mmean2 = 0;
		for(int i = (int)(((lt.x-columnOffset)+((rt.x-lt.x)*3)/4)); i < rt.x-columnOffset; i++) {
			mmean2 += horizon[i];
		}
		mmean2 /= ((rt.x-lt.x)/4);
		lb.x = lt.x;
		lb.y = mmean1;
		rb.x = rt.x;
		rb.y = mmean2;

		if(Math.abs(rt.y-mean) > 2*Math.abs(lt.y-mean))
			rt.y = lt.y+(Math.abs(rt.y-mean)/2);
		if(Math.abs(lt.y-mean) > 2*Math.abs(rt.y-mean))
			lt.y = rt.y+(Math.abs(lt.y-mean)/2);

		Scalar cl = new Scalar(EDGE_PIXELS_2);
		Core.line(originalImage, lt, rt, cl, 2);
		Core.line(originalImage, rt, rb, cl, 2);
		Core.line(originalImage, rb, lb, cl, 2);
		Core.line(originalImage, lb, lt, cl, 2);
		Highgui.imwrite(outputImageFile, originalImage);
		originalImage.release();
	}
	
	public static void localizePadInDir(String originalDir, String grassDir, String outputDir) {
		File originalFolder = new File(originalDir);
		File bwFolder = new File(grassDir);

		File[] originalFiles = originalFolder.listFiles();
		File[] bwFiles = bwFolder.listFiles();

		if(originalFiles.length != bwFiles.length) return;

		Arrays.sort(originalFiles);
		Arrays.sort(bwFiles);

		for(int i = 0; i < originalFiles.length; i++) {
			PadLocalizer2.localizePad(originalFiles[i].getPath(), bwFiles[i].getPath(), outputDir+originalFiles[i].getName());
		}
	}

	static double findJump(double[] strip, int iterations) {
		int beginIndex = strip.length;
		for(int i = 0; i < iterations; i++)
			beginIndex /= 2;
		int endIndex = beginIndex*2, resultIndex = 0, thresh = 10;
		for(int i = beginIndex; i < endIndex; i+=6) {
			if(Math.abs(strip[i]) <= thresh && Math.abs(strip[i+1]) <= thresh && Math.abs(strip[i+2]) <= thresh && Math.abs(strip[i+3]) <= thresh && Math.abs(strip[i+4]) <= thresh && Math.abs(strip[i+5]) <= thresh && Math.abs(strip[i+6]) <= thresh) {
				resultIndex = i-beginIndex;
				break;
			}
		}

		for(int i = 0; i < iterations; i++)
			resultIndex *= 2;
		return resultIndex;
	}
}