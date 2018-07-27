package Problem;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class PadLocalizer {
	public static int numOfCols = 0;
	public static int numOfRows = 0;

	public static void main(String[] args) {
		localizePad("2015-07-27_17-42-44.png", "2015-07-27_17-42-44b.png", "2015-07-27_17-42-44.png");
	}

	public static void localizePad(String originalImageFile, String grassImageFile, String outputImageFile) {
		EdgeDetection.markFHWTEdgeHillTopsInRowsandCols(grassImageFile, outputImageFile);
		Mat hwtImg = Highgui.imread("out\\"+outputImageFile);
		Imgproc.threshold(hwtImg,hwtImg, 200, 255, Imgproc.THRESH_BINARY);
		Mat grayscale = new Mat(PadLocalizer.numOfRows, PadLocalizer.numOfCols, CvType.CV_8UC1);
		Imgproc.cvtColor(hwtImg, grayscale, Imgproc.COLOR_RGB2GRAY);
		Map<Integer, Integer> colCount = new HashMap<Integer, Integer>();
		int count=0;
		for (int col = 0; col < PadLocalizer.numOfCols; col++) {
			for (int row = 0; row < PadLocalizer.numOfRows; row++) {
				if(grayscale.get(row, col)[0]>=200.0)
					count++;
			}
			colCount.put(col, count);
		}

		Map<Integer, Integer> sumCol = new HashMap<Integer, Integer>();
		Mat histImg = new Mat(numOfRows, numOfCols, CvType.CV_8UC1);
		Imgproc.cvtColor(hwtImg, histImg, Imgproc.COLOR_RGB2GRAY);

		for(int i=0;i<colCount.size()-5;i+=5) {
			sumCol.put(i, colCount.get(i)+colCount.get(i+1)+colCount.get(i+2)+colCount.get(i+3)+colCount.get(i+4));
		}

		Map<Integer, Integer> rowCount = new HashMap<Integer, Integer>();
		count=0;
		for (int row = 0; row < PadLocalizer.numOfRows; row++) {
			for (int col = 0; col < PadLocalizer.numOfCols; col++) {
				if(grayscale.get(row, col)[0]>=200.0)
					count++;
			}
			rowCount.put(row, count);
		}

		Map<Integer, Integer> sumRow = new HashMap<Integer, Integer>();
		for(int i=0;i<rowCount.size()-5;i+=5) {
			sumRow.put(i, rowCount.get(i)+rowCount.get(i+1)+rowCount.get(i+2)+rowCount.get(i+3)+rowCount.get(i+4));
		}

		for(int i=0;i<sumCol.size()-5;i+=5) {
			if(Math.abs(sumCol.get(i)-sumCol.get(i+5))>=10) {
				final double[] EDGE = {0, 255, 0};
				Scalar color = new Scalar(EDGE);
				Point topleft = new Point(i, 0);
				Point topright  = new Point(i+5, 0);
				Point bottomleft = new Point(i, numOfRows);
				Point bottomright = new Point(i+5, numOfRows);
				Core.line(histImg, topleft, topright, color, 2);
				Core.line(histImg, topright, bottomright, color, 2);
				Core.line(histImg, bottomright, bottomleft, color, 2);
				Core.line(histImg, bottomleft, topleft, color, 2);

			}
		}

		for(int i=0;i<sumRow.size()-5;i+=5) {
			if(Math.abs(sumRow.get(i)-sumRow.get(i+5))>=10) {
				final double[] EDGE = {0, 255, 0};
				Scalar color = new Scalar(EDGE);
				Point topleft = new Point(i, 0);
				Point topright  = new Point(i+5, 0);
				Point bottomleft = new Point(i, numOfCols);
				Point bottomright = new Point(i+5, numOfCols);
				Core.line(histImg, topleft, topright, color, 2);
				Core.line(histImg, topright, bottomright, color, 2);
				Core.line(histImg, bottomright, bottomleft, color, 2);
				Core.line(histImg, bottomleft, topleft, color, 2);

			}
		}

		Highgui.imwrite("out\\"+outputImageFile, histImg);
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
}
