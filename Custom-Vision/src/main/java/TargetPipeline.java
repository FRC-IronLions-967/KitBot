import edu.wpi.first.vision.VisionPipeline;

import java.util.*;

import org.opencv.core.*;
import org.opencv.imgproc.*;

public class TargetPipeline implements VisionPipeline {
  public static Mat drawing;
  public static volatile int val;
  public volatile double tx;
  public volatile double ty;
  public volatile double reliability;
  public volatile double contourArea;
  public volatile double area;
  public volatile double areaProp;
  public volatile double height;
  public volatile double width;
  public volatile boolean hasTarget = false;
  public final double MIN_AREA_PROP = 0.17;
  public final double MAX_AREA_PROP = 0.30;

  public volatile Mat result;

  @Override
  public void process(Mat mat) {

    val++;
    
    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);

    Imgproc.blur(mat, mat, new Size(3, 3));

    Imgproc.threshold(mat, mat, 235, 255, 0);

    Imgproc.Canny(mat, mat, 50, 200, 3, false);

    List<MatOfPoint> contours = new ArrayList<>();
    Mat hierarchy = new Mat();

    Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

    MatOfPoint2f[] contoursPoly = new MatOfPoint2f[contours.size()];
    Rect boundRects[] = new Rect[contours.size()];

    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2BGR);

    for (int i = 0; i < contours.size(); i++) {
      contoursPoly[i] = new MatOfPoint2f();
      Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), contoursPoly[i], 3, true);
      boundRects[i] = Imgproc.boundingRect(contoursPoly[i]);
    }

    if (boundRects.length > 0) {
  
      ArrayList<Rect> targets = new ArrayList<>();
      for (int i = 0; i < boundRects.length; i++) {
          contourArea = Imgproc.contourArea(contours.get(i));
          area = boundRects[i].area();
          if(contourArea / area < MAX_AREA_PROP && contourArea / area > MIN_AREA_PROP && area > 50) {
            targets.add(boundRects[i]);
          }
      }

      System.out.println("Number of targets: " + targets.size());
      if(targets.size() > 0) {

        int maxIndex = 0;

        for(int i = 1; i < targets.size(); i++) {
          maxIndex = (targets.get(i).area() > targets.get(maxIndex).area()) ? i : maxIndex;
        }

        Rect bRect = targets.get(maxIndex);
        Imgproc.rectangle(mat, bRect.tl(), bRect.br(), new Scalar(0, 0, 255));
        tx = ((double)bRect.tl().x - (((double)mat.cols() / 2.0) - (double)bRect.width / 2.0));
        ty = ((((double)mat.rows() / 2.0) - (double)bRect.height / 2.0) - (double)bRect.tl().y);
        height = (double) bRect.height;
        width = (double) bRect.width;
        area = bRect.area();
        areaProp = contourArea / area;
        reliability = (width > 15.0) ? 1.0 : 0.0;
        hasTarget = true;
      } else {
        reliability = 0.0;
        hasTarget = false;
      }
    }

    Imgproc.line(mat, new Point(320, 80), new Point(320, 100), new Scalar(0, 255, 0), 2, Imgproc.LINE_8, 0);
    Imgproc.line(mat, new Point(310, 90), new Point(330, 90), new Scalar(0, 255, 0), 2, Imgproc.LINE_8, 0);
    Imgproc.line(mat, new Point(320, 110), new Point(320, 440), new Scalar(0, 255, 0), 2, Imgproc.LINE_8, 0);
    for(int i = 110; i < 440; i += 30) {
      Imgproc.line(mat, new Point(310, i), new Point(330, i), new Scalar(0, 255, 0), 2, Imgproc.LINE_8, 0);
    }

    this.result = mat.clone();
  }
}