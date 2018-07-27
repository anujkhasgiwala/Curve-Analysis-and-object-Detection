package Problem;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import Problem.OneDHWT;
import Problem.Utils;

/**
 * ImageManip.java
 * Some image processing methods covered in
 * CS 6810/7810: Lecture 3
 * To run the tests covered in class uncomment them in main().
 * 
 * @author Vladimir Kulyukin
 */
public class ImageManip {
	
	//File paths
    static final String IMAGE_SOURCE_DIR = "HW4_Images\\";

    // Change this variable accordingly
    static final String OPENCV_DLL_PATH
    = "D:\\workspace\\USU-Assignments\\CS 6810 - Wavelets and Wavelet Algorithms\\Assignment 4\\Assignment 4\\src\\Problem\\opencv_java2413.dll";

    // Use a static code block to load the dll/so;
    static {
    System.load(OPENCV_DLL_PATH);
    }
    
    // get pix values from the image
    public static void getPixValues(String img_path) {
    Mat orig = Highgui.imread(IMAGE_SOURCE_DIR + img_path);
    if (orig.rows() == 0 || orig.cols() == 0) {
    throw new IllegalArgumentException("Failed to read " 
    + IMAGE_SOURCE_DIR + img_path);
    }
    for (int row = 0; row < orig.rows(); row++) {
    for (int col = 0; col < orig.cols(); col++) {
    double[] pix = orig.get(row, col);
    System.out.print("[" + pix[0] + ", "
        + pix[1] + ", " + pix[2] + "] ");
    }
    System.out.println();
    }
    orig.release();
    }
    
    // get the 1st pix value from a 3-element pixel
    public static void get1CPixVals(String infile) {
    Mat orig = Highgui.imread(IMAGE_SOURCE_DIR + infile);
    if (orig.rows() == 0 || orig.cols() == 0) {
    throw new IllegalArgumentException("Failed to read " 
    + IMAGE_SOURCE_DIR + infile);
    }
    double[] row_pix = new double[orig.rows()];
    for (int row = 0; row < orig.rows(); row++) {
    for (int col = 0; col < orig.cols(); col++) {
    row_pix[col] = orig.get(row, col)[0];
    }
    for (int col = 0; col < orig.cols(); col++) {
    System.out.print(row_pix[col] + " ");
    }
    System.out.println();
    }
    orig.release();
    }
    
    // Edges are typically detected in grayscale images. Here is how you can convert
    // images to grayscale. CvType.CV_8UC1 – means 8-bit, unsigned, 1 channel.
    // It is the type that defines grayscale images and is used for many CV algorithms.
    public static void grayscaleImage(String infile, String outfile) {
    Mat original = Highgui.imread(IMAGE_SOURCE_DIR + infile);
    if (original.rows() == 0 || original.cols() == 0) {
    throw new IllegalArgumentException("Failed to read " + IMAGE_SOURCE_DIR + infile);
    }
    Mat grayscale = new Mat(original.rows(), original.cols(), CvType.CV_8UC1);
    Imgproc.cvtColor(original, grayscale, Imgproc.COLOR_RGB2GRAY);
    Highgui.imwrite(IMAGE_SOURCE_DIR + outfile, grayscale);
    // When we are done using Mat objects, they should be released.
    original.release();
    grayscale.release();
    }
    
    // This is how we can apply 1 iteration of 1D FHWT to each image row.
    public static void applyRowBasedFHWT(String infile) {
    Mat orig = Highgui.imread(IMAGE_SOURCE_DIR + infile);
    double[] row_pix = new double[orig.rows()];
    if (orig.rows() == 0 || orig.cols() == 0) {
    throw new IllegalArgumentException("Failed to read " + IMAGE_SOURCE_DIR + infile);
    }
    for (int row = 0; row < orig.rows(); row++) {
    for (int col = 0; col < orig.cols(); col++) {
    row_pix[col] = orig.get(row, col)[0];
    }
    System.out.print("Orig: ");
    for (int col = 0; col < orig.cols(); col++) {
    System.out.print(row_pix[col] + "\t");
    }
    OneDHWT.inPlaceFastHaarWaveletTransformForNumIters(row_pix, 1);
    System.out.println();
    System.out.print("FHWT: ");
    for (int col = 0; col < orig.cols(); col++) {
    System.out.print(row_pix[col] + "\t");
    }
    System.out.println();
    }
    orig.release();
    }
    
    public final static double WAVELET_COEFF_THRESH = 20.0;
    public final static double EDGE_MARK = 255.0;
    public final static double NO_EDGE_MARK = 0.0;
    public final static double[] NO_EDGE_PIX = {NO_EDGE_MARK, NO_EDGE_MARK, NO_EDGE_MARK};
    public final static double[] YES_EDGE_PIX = {EDGE_MARK, EDGE_MARK, EDGE_MARK};
    
    public static void markFHWTEdgeHillTopsInRows(String infile, String outfile) {
    Mat orig = Highgui.imread(IMAGE_SOURCE_DIR + infile);
    if (orig.rows() == 0 || orig.cols() == 0) {
    throw new IllegalArgumentException("Failed to read " + IMAGE_SOURCE_DIR + infile);
    }
    final int num_rows = orig.rows();
    final int num_cols = orig.cols();
    Mat grayscale = new Mat(num_rows, num_cols, CvType.CV_8UC1);
    Imgproc.cvtColor(orig, grayscale, Imgproc.COLOR_RGB2GRAY);
    double[] fhwt_row_pix = new double[num_rows];

    for (int row = 0; row < num_rows; row++) {
    for (int col = 0; col < num_cols; col++) {
    fhwt_row_pix[col] = grayscale.get(row, col)[0];
    }
    OneDHWT.inPlaceFastHaarWaveletTransformForNumIters(fhwt_row_pix, 1);
    for (int col = 1; col < num_cols; col += 2) {
    if (Math.abs(fhwt_row_pix[col]) >= WAVELET_COEFF_THRESH) {
    if (fhwt_row_pix[col] < 0) {
        orig.put(row, col, YES_EDGE_PIX);
        orig.put(row, col - 1, NO_EDGE_PIX);
    } else if (fhwt_row_pix[col] > 0) {
        orig.put(row, col, NO_EDGE_PIX);
        orig.put(row, col - 1, YES_EDGE_PIX);
    }
    } else {
    orig.put(row, col, NO_EDGE_PIX);
    orig.put(row, col - 1, NO_EDGE_PIX);
    }
    }
    }
    Highgui.imwrite(IMAGE_SOURCE_DIR + outfile, orig);
    orig.release();
    }

    // The methods below are various utils like creating black mats,
    // drawing lines in mats, etc.
    public static void drawLineInMat(Mat mat, int start_x, int start_y,
    int end_x, int end_y, Scalar color, int line_width) {
    Point start_point = new Point(start_x, start_y);
    Point end_point = new Point(end_x, end_y);
    Core.line(mat, start_point, end_point, color, line_width);
    }

    public static Mat createBlackMat(int num_rows, int num_cols) {
    Mat mat = new Mat(num_rows, num_cols, CvType.CV_8UC1);
    double[] color = {0, 0, 0};
    for (int row = 0; row < num_rows; row++) {
    for (int col = 0; col < num_cols; col++) {
    mat.put(row, col, color);
    }
    }
    return mat;
    }

    public static Mat convert2DArrayToGrayscaleMat(double[][] ary) {
    final int num_rows = ary[0].length;
    final int num_cols = ary.length;
    Mat mat = new Mat(num_rows, num_cols, CvType.CV_8UC1);
    double[] color = {0, 0, 0};
    for (int row = 0; row < num_rows; row++) {
    for (int col = 0; col < num_cols; col++) {
    color[0] = ary[row][col];
    color[1] = ary[row][col];
    color[2] = ary[row][col];
    mat.put(row, col, color);
    }
    }
    return mat;
    }

    public static void save2DArrayAsGrayscaleMat(double[][] ary, String image_path) {
    final int num_rows = ary[0].length;
    final int num_cols = ary.length;
    Mat mat = new Mat(num_rows, num_cols, CvType.CV_8UC1);
    double[] color = {0, 0, 0};
    for (int row = 0; row < num_rows; row++) {
    for (int col = 0; col < num_cols; col++) {
    color[0] = ary[row][col];
    color[1] = ary[row][col];
    color[2] = ary[row][col];
    mat.put(row, col, color);
    }
    }
    Highgui.imwrite(image_path, mat);
    mat.release();
    }

    public static void drawLineInMat(Mat mat, Point start_point, Point end_point,
    Scalar color, int line_width) {
    Core.line(mat, start_point, end_point, color, line_width);
    }

    public static void createBlackMatWithWhiteLine(String img_path, int num_rows, int num_cols, int start_x, int start_y,
    int end_x, int end_y, int line_width) {
    Mat mat = createBlackMat(num_rows, num_cols);
    drawLineInMat(mat, start_x, start_y, end_x, end_y, new Scalar(255, 255, 255), line_width);
    Highgui.imwrite(img_path, mat);
    mat.release();
    }

    // image is assumed to be grayscale
    public static double[][] convertGrayscaleMatTo2DArray(String img_path) {
    Mat mat = Highgui.imread(img_path);
    double[][] ary = new double[mat.rows()][mat.cols()];
    for (int row = 0; row < mat.rows(); row++) {
    for (int col = 0; col < mat.cols(); col++) {
    ary[row][col] = mat.get(row, col)[0];
    }
    }
    mat.release();
    return ary;
    }

    public static double[][] convertMatTo2DGrayscaleArray(String img_path) {
    Mat mat = Highgui.imread(img_path, CvType.CV_8UC1);
    double[][] ary = new double[mat.rows()][mat.cols()];
    for (int row = 0; row < mat.rows(); row++) {
    for (int col = 0; col < mat.cols(); col++) {
    ary[row][col] = mat.get(row, col)[0];
    }
    }
    mat.release();
    return ary;
    }

    public static double[] convertGrayscaleMatRowTo1DArray(String img_path, int row, int start_col, int end_col) {
    Mat mat = Highgui.imread(img_path);
    if (mat.empty()) {
    System.out.println("Mat empty");
    mat.release();
    return null;
    }
    if (mat.rows() <= row || start_col < 0 || mat.cols() <= end_col) {
    System.out.println("mat.rows() <= row || start_col < 0 || mat.cols() <= end_col");
    mat.release();
    return null;
    }
    final double[] rowary = new double[end_col - start_col + 1];
    System.out.println("mat.cols() == " + mat.cols());
    System.out.println("rowary.length = " + rowary.length);
    for (int col = start_col, i=0; col <= end_col; col++, i++) {
    rowary[i] = mat.get(row, col)[0];
    }
    mat.release();
    return rowary;
    }

    // image is assumed to be grayscaled
    public static double[] convertGrayscaleMatColTo1DArray(String img_path, int col, int start_row, int end_row) {
    Mat mat = Highgui.imread(img_path);
    if ( mat.empty() ) {
    mat.release();
    return null;
    }
    if ( mat.cols() <= col || start_row < 0 || mat.rows() <= end_row ) {
    mat.release();
    return null;
    }

    final double[] colary = new double[end_row - start_row + 1];
    for (int row = start_row, i = 0; row <= end_row; row++, i++) {
    //System.out.println("row = " + row + "col = " + col);
    colary[i] = mat.get(row, col)[0];
    }
    mat.release();
    return colary;
    }

    public static void createBlackMatsWithWhiteHorLines(int img_size) {
    // hor lines: y is a row
    for (int y = 0; y < img_size; y++) {
    final String imgPath = IMAGE_SOURCE_DIR + "hor_lines/hor_line_img" + img_size
    + "x" + img_size + "_" + y + ".JPG";
    createBlackMatWithWhiteLine(imgPath,
    img_size, img_size, 0, y, img_size - 1, y, 1);
    double[][] orig = convertGrayscaleMatTo2DArray(imgPath);
    System.out.println("original mat");
    Utils.display2DArray(orig, img_size, img_size);
    System.out.println("=======");
    }

    }

    public static void createBlackMatsWithWhiteVerLines(int img_size) {
    // hor lines: x is a row
    for (int x = 0; x < img_size; x++) {
    final String imgPath = IMAGE_SOURCE_DIR + "ver_lines/ver_line_img" + img_size
    + "x" + img_size + "_" + x + ".JPG";
    createBlackMatWithWhiteLine(imgPath,
    img_size, img_size, x, 0, x, img_size - 1, 1);
    double[][] orig = convertGrayscaleMatTo2DArray(imgPath);
    System.out.println("original mat");
    Utils.display2DArray(orig, img_size, img_size);
    System.out.println("=======");
    }
    }

    public static void createBlackMatsWithWhiteTopLeftBotRightDigLines(int img_size) {
    for (int x = 0; x < img_size; x++) {
    int start_x = x, start_y = 0, end_x = img_size - 1, end_y = img_size - x - 1;
    final String imgPath = IMAGE_SOURCE_DIR
    + "dig_lines/dig_top_left_bot_right_line_img" + img_size
    + "x" + img_size + "_" + "_" + start_x + "_" + start_y
    + "_" + end_x + "_" + end_y + ".JPG";
    createBlackMatWithWhiteLine(imgPath,
    img_size, img_size, start_x, start_y, end_x, end_y, 1);
    double[][] orig = convertGrayscaleMatTo2DArray(imgPath);
    System.out.println("original mat");
    Utils.display2DArray(orig, img_size, img_size);
    System.out.println("=======");
    }
    for (int y = 0; y < img_size; y++) {
    int start_x = 0, start_y = y, end_x = img_size - y - 1, end_y = img_size - 1;
    final String imgPath = IMAGE_SOURCE_DIR
    + "dig_lines/dig_top_left_bot_right_line_img" + img_size
    + "x" + img_size + "_" + "_" + start_x + "_" + start_y
    + "_" + end_x + "_" + end_y + ".JPG";
    createBlackMatWithWhiteLine(imgPath,
    img_size, img_size, start_x, start_y, end_x, end_y, 1);
    double[][] orig = convertGrayscaleMatTo2DArray(imgPath);
    System.out.println("original mat");
    Utils.display2DArray(orig, img_size, img_size);
    System.out.println("=======");
    }
    }

    public static void createBlackMatsWithWhiteTopRightBotLeftDigLines(int img_size) {
    for (int x = img_size - 1; x >= 0; x--) {
    int start_x = x, start_y = 0, end_x = 0, end_y = x;
    final String imgPath = IMAGE_SOURCE_DIR
    + "dig_lines/dig_top_right_bot_left_line_img" + img_size
    + "x" + img_size + "_" + "_" + start_x + "_" + start_y
    + "_" + end_x + "_" + end_y + ".JPG";
    createBlackMatWithWhiteLine(imgPath,
    img_size, img_size, start_x, start_y, end_x, end_y, 1);
    double[][] orig = convertGrayscaleMatTo2DArray(imgPath);
    System.out.println("original mat");
    Utils.display2DArray(orig, img_size, img_size);
    System.out.println("=======");
    }

    final int start_x = img_size - 1;
    for (int y = 1; y < img_size; y++) {
    int start_y = y, end_x = y, end_y = img_size - 1;
    System.out.print("start_x = " + start_x + " start_y = " + start_y);
    System.out.println(" end_x = " + end_x + " end_y = " + end_y);
    final String imgPath = IMAGE_SOURCE_DIR
    + "dig_lines/dig_top_right_bot_left_line_img" + img_size
    + "x" + img_size + "_" + "_" + start_x + "_" + start_y
    + "_" + end_x + "_" + end_y + ".JPG";
    createBlackMatWithWhiteLine(imgPath,
    img_size, img_size, start_x, start_y, end_x, end_y, 1);
    double[][] orig = convertGrayscaleMatTo2DArray(imgPath);
    System.out.println("original mat");
    Utils.display2DArray(orig, img_size, img_size);
    System.out.println("=======");
    }
    }

    
    public static void generateTestImage00() {
    ImageManip.createBlackMatWithWhiteLine(IMAGE_SOURCE_DIR + "test_00.jpg", 
    8, 8, 0, 0, 7, 7, 1);    
    }
    
    public static void generateTestImage01() {
    ImageManip.createBlackMatWithWhiteLine(IMAGE_SOURCE_DIR + "test_01.jpg", 
    8, 8, 0, 7, 7, 0, 1);    
    }
    
    public static void generateTestImage02() {
    ImageManip.createBlackMatWithWhiteLine(IMAGE_SOURCE_DIR + "test_02.jpg", 
    8, 8, 4, 0, 4, 7, 1);    
    }
    
    public static void generateTestImage03() {
    ImageManip.createBlackMatWithWhiteLine(IMAGE_SOURCE_DIR + "test_03.jpg", 
    8, 8, 0, 4, 7, 4, 1);    
    }
  
    // uncomment these tests to run examples covered in class.
    public static void main(String[] args) {
    	/*ImageManip.getPixValues("test_00.jpg");
    ImageManip.get1CPixVals("test_00.jpg");
    ImageManip.grayscaleImage("salt_of_the_earth.jpg", "salt_of_the_earth_gsl.jpg");
    ImageManip.applyRowBasedFHWT("test_00.jpg");
    ImageManip.markFHWTEdgeHillTopsInRows("rings.jpg", "rings_edges.jpg");*/
    	EdgeDetection.applyColumnBasedFHWT("test_02.jpg", "test_021.jpg");
    	//EdgeDetection.markFHWTEdgeHillTopsInCols("rings.jpg", "rings_edges.jpg");
    }
}