package image.compressor;

import org.imgscalr.Scalr;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Compressor {
    private File inputPath;
    private File outputPath;
    private int targetWidth;

    public static void main(String[] args) {
        Compressor comp = new Compressor("E:/Java Projects/Netflex/thumbnails", "E:/Java Projects/Netflex/compressed", 400);
        for (File f : comp.inputPath.listFiles()) {
            try {
                comp.saveThumb(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Compressor(String inputPath, String outputPath, int targetWidth) {
        this.inputPath = new File(inputPath);
        this.outputPath = new File(outputPath);
        this.targetWidth = targetWidth;
    }

    public void saveThumb(File filename) throws IOException {
        BufferedImage imgIn = ImageIO.read(filename);
        BufferedImage imgOut;
        if (targetWidth <= imgIn.getWidth())
            imgOut = centerCrop(imgIn, targetWidth, 16f / 9f);
        else
            imgOut = centerCrop(imgIn, imgIn.getWidth(), 16f / 9f);

        System.out.println(filename.getName());

        File outputfile = new File(outputPath.getAbsolutePath() + "/" + filename.getName());
        ImageIO.write(imgOut, "jpg", outputfile);
    }


    public BufferedImage centerCrop(BufferedImage image, int targetWidth, float aspectRatio) {
        int[] crop_filter;
        if((double)image.getWidth()/(double)image.getHeight()>aspectRatio) {
            int new_width = (int) Math.round((double) image.getHeight() * aspectRatio);
            crop_filter = new int[]{(image.getWidth() - new_width) / 2, 0, new_width, image.getHeight()};
        } else {
            int new_height = (int) Math.round((double) image.getWidth() / aspectRatio);
            crop_filter = new int[]{0, (image.getHeight() - new_height) / 2, image.getWidth(), new_height};
        }
        BufferedImage cropped = Scalr.crop(image, crop_filter[0], crop_filter[1], crop_filter[2], crop_filter[3], Scalr.OP_ANTIALIAS);
        return Scalr.resize(cropped, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, targetWidth, Math.round((float)targetWidth/aspectRatio), Scalr.OP_ANTIALIAS);
    }

}
