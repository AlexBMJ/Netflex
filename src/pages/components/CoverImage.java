package pages.components;

import content.Content;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class CoverImage extends ImageView {

    private Content MovieInfo;
    private float aspectRatio;

    public CoverImage(Image img, Content result, float aspectRatio) {
        super(img);
        MovieInfo = result;
        this.aspectRatio = aspectRatio;
    }

    public Content getInfo() {return MovieInfo;}

    public Runnable fetchImage = () -> {
        Image image = MovieInfo.getImage();
        int[] cropSize = cropAspectRatio(image);
        Rectangle2D croppedPortion = new Rectangle2D(cropSize[0], cropSize[1], cropSize[2], cropSize[3]);
        Platform.runLater(() -> {this.setViewport(croppedPortion);this.setImage(image);});
    };

    private int[] cropAspectRatio(Image image) {
        int[] crop_filter;
        if(image.getWidth() / image.getHeight() >aspectRatio) {
            int new_width = (int) Math.round(image.getHeight() * aspectRatio);
            crop_filter = new int[]{(int)Math.round(image.getWidth() - new_width) / 2, 0, new_width, (int)Math.round(image.getHeight())};
        } else {
            int new_height = (int) Math.round(image.getWidth() / aspectRatio);
            crop_filter = new int[]{0, (int)Math.round(image.getHeight() - new_height) / 2, (int)Math.round(image.getWidth()), new_height};
        }
        return crop_filter;
    }



}
