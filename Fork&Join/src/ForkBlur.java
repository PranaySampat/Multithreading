import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import javax.imageio.ImageIO;

/**
 * ForkBlur implements a simple horizontal image blur. It averages pixels in the
 * source array and writes them to a destination array. The sThreshold value
 * determines whether the blurring will be performed directly or split into two
 * tasks.
 *
 * This is not the recommended way to blur images; it is only intended to
 * illustrate the use of the Fork/Join framework.
 */
public class ForkBlur extends RecursiveAction {

	private int[] mSource;
	private int mStart;
	private int mLength;
	private int[] mDestination;
	private int mBlurWidth = 15; // Processing window size, should be odd.

	public ForkBlur(int[] src, int start, int length, int[] dst) {
		mSource = src;
		mStart = start;
		mLength = length;
		mDestination = dst;
	}

	// Average pixels from source, write results into destination.
	protected void computeDirectly() {
		int sidePixels = (mBlurWidth - 1) / 2;
		for (int index = mStart; index < mStart + mLength; index++) {
			// Calculate average.
			float rt = 0, gt = 0, bt = 0;
			for (int mi = -sidePixels; mi <= sidePixels; mi++) {
				int mindex = Math.min(Math.max(mi + index, 0), mSource.length - 1);
				int pixel = mSource[mindex];
				rt += (float) ((pixel & 0x00ff0000) >> 16) / mBlurWidth;
				gt += (float) ((pixel & 0x0000ff00) >> 8) / mBlurWidth;
				bt += (float) ((pixel & 0x000000ff) >> 0) / mBlurWidth;
			}

			// Re-assemble destination pixel.
			int dpixel = (0xff000000) | (((int) rt) << 16) | (((int) gt) << 8) | (((int) bt) << 0);
			mDestination[index] = dpixel;
		}
	}

	protected static int sThreshold = 10000;

	@Override
	protected void compute() {
		if (mLength < sThreshold) {
			computeDirectly();
			return;
		}

		int split = mLength / 2;

		invokeAll(new ForkBlur(mSource, mStart, split, mDestination),
				new ForkBlur(mSource, mStart + split, mLength - split, mDestination));
	}

	// Plumbing follows.
	public static void main(String[] args) throws Exception {
		String srcName = "image.jpg";
		File srcFile = new File(srcName);
		BufferedImage image = ImageIO.read(srcFile);

		System.out.println("Source image: " + srcName);
		int processors = Runtime.getRuntime().availableProcessors();
		System.out.println(
				Integer.toString(processors) + " processor" + (processors != 1 ? "s are " : " is ") + "available");
		
		// Serial Blur
		long startTime = System.currentTimeMillis();
		int w = image.getWidth();
		int h = image.getHeight();
		int[] src = image.getRGB(0, 0, w, h, null, 0, w);
		System.out.println("Array size is " + src.length);
		System.out.println("Threshold is " + sThreshold);

		BufferedImage serialBlurImage = serialBlur(w, h, src);
		long endTime = System.currentTimeMillis();
		System.out.println("Serial Blur Processing time : " + (endTime - startTime) + " milliseconds.");
		saveImage(serialBlurImage, "seriallyimage.jpg");
		
		// no of proecessors
		BufferedImage blurredImage = blurParallel(image, processors + 0);
		saveImage(blurredImage, "image1blur.jpg");
		
		// no of proecessors + 2
		BufferedImage blurredImage1 = blurParallel(image, processors + 2);
		saveImage(blurredImage1, "image2blur.jpg");
		
		// no of proecessors + 4
		BufferedImage blurredImage2 = blurParallel(image, processors + 4);
		saveImage(blurredImage2, "image3blur.jpg");
	}

	public static void saveImage(BufferedImage image, String dstName) throws Exception {
		File dstFile = new File(dstName);
		ImageIO.write(image, "jpg", dstFile);
		System.out.println("Output image: " + dstName);
	}

	public static BufferedImage blurParallel(BufferedImage image, int processors) {
		long startTime = System.currentTimeMillis();
		int w = image.getWidth();
		int h = image.getHeight();
		int[] src = image.getRGB(0, 0, w, h, null, 0, w);
		int[] dst = new int[src.length];
		ForkBlur fb = new ForkBlur(src, 0, src.length, dst);
		ForkJoinPool pool = new ForkJoinPool(processors);
		pool.invoke(fb);
		BufferedImage blurredimage = serialBlur(w, h, dst);
		long endTime = System.currentTimeMillis();
		System.out.println(processors + " processors took "+ (endTime - startTime)
				+ " ms.");
		return blurredimage;
	}

	public static BufferedImage serialBlur(int w, int h, int[] src) {
		int[] dst = new int[src.length];
		ForkBlur fb = new ForkBlur(src, 0, src.length, dst);
		fb.computeDirectly();
		BufferedImage dstImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		dstImage.setRGB(0, 0, w, h, fb.mDestination, 0, w);
		return dstImage;
	}

}