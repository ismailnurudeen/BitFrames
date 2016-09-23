package proj.me.bitframe;

import android.graphics.Bitmap;
import android.view.View;

import java.util.List;

/**
 * Created by root on 23/9/16.
 */

public abstract class ImageShades implements ImageClickHandler{
    ImageCallback imageCallback;
    void setImageCallback(ImageCallback imageCallback){
        this.imageCallback = imageCallback;
    }

    boolean result;
    void setResult(boolean result){
        this.result = result;
    }

    protected boolean getResult(){
        return result;
    }

    protected void addImageView(View view, int viewWidth, int viewHeight, boolean hasAddInLayout) {
        imageCallback.addImageView(view, viewWidth, viewHeight, hasAddInLayout);
    }

    protected void imageClicked(ImageType imageType, int imagePosition, String imageLink) {
        imageCallback.imageClicked(imageType, imagePosition, imageLink);
    }

    protected void setColorsToAddMoreView(int resultColor, int mixedColor, int invertedColor) {
        imageCallback.setColorsToAddMoreView(resultColor, mixedColor, invertedColor);
    }

    protected void frameResult(BeanBitFrame... beanBitFrames) {
        imageCallback.frameResult(beanBitFrames);
    }

    protected void addMore() {
        imageCallback.addMore();
    }

    protected abstract void updateFrameUi(List<Bitmap> images, List<BeanImage> beanImages, boolean hasImageProperties);

}