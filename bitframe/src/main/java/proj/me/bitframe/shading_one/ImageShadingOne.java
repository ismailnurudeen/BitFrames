package proj.me.bitframe.shading_one;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import proj.me.bitframe.BeanBitFrame;
import proj.me.bitframe.BeanImage;
import proj.me.bitframe.FrameModel;
import proj.me.bitframe.ImageShades;
import proj.me.bitframe.ImageType;
import proj.me.bitframe.R;
import proj.me.bitframe.databinding.ViewSingleBinding;
import proj.me.bitframe.dimentions.BeanShade1;
import proj.me.bitframe.exceptions.FrameException;
import proj.me.bitframe.helper.Utils;


/**
 * Created by Deepak.Tiwari on 28-09-2015.
 */
public final class ImageShadingOne extends ImageShades{
    Context context;
    LayoutInflater inflater;
    int totalImages;

    BeanBitFrame beanBitFrame1;
    String imageLink1;

    boolean result;

    BindingShadeOne shadingOneBinding;
    FrameModel frameModel;

    public ImageShadingOne(Context context, int totalImages, FrameModel frameModel){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.totalImages = totalImages;

        beanBitFrame1 = new BeanBitFrame();
        beanBitFrame1.setLoaded(true);

        this.frameModel = frameModel;
    }

    /**
     * should be called on any single image ui, either failed so it can show a default image set in the utils
     * or the loaded image via piccaso
     * */

    @Override
    protected void updateFrameUi(List<Bitmap> bitmaps, List<BeanImage> beanImages, boolean hasImageProperties) throws FrameException{
        if(beanImages == null || beanImages.size() == 0) throw new IllegalArgumentException("BeanImage list should not be null or empty");
        BeanBitFrame beanBitFrame = null;
        if(hasImageProperties) beanBitFrame = (BeanBitFrame) beanImages.get(0);
        result = getResult();
        imageLink1 = beanImages.get(0).getImageLink();

        Bitmap bitmap = bitmaps == null || bitmaps.size() == 0 ? null : bitmaps.get(0);

        beanBitFrame1.setPrimaryCount(beanImages.get(0).getPrimaryCount());
        beanBitFrame1.setSecondaryCount(beanImages.get(0).getSecondaryCount());

        shadingOneBinding = new BindingShadeOne();

        ViewSingleBinding viewSingleBinding = DataBindingUtil.bind(inflater.inflate(R.layout.view_single, null));

        viewSingleBinding.setClickHandler(this);
        viewSingleBinding.setShadeOne(shadingOneBinding);


        //image counter
        shadingOneBinding.setImageCounterVisibility(false);

        //comments
        shadingOneBinding.setShouldCommentVisible(frameModel.isShouldShowComment());


        int width, height;
        if(!result){
            width =0;height=0;
            shadingOneBinding.setImageCounterVisibility(true);
            shadingOneBinding.setImageCounterText("+"+totalImages);
        }else {
            width = hasImageProperties ? (int)beanBitFrame.getWidth() : bitmap.getWidth();
            height = hasImageProperties ? (int)beanBitFrame.getHeight() : bitmap.getHeight();

            shadingOneBinding.setComment(beanImages.get(0).getImageComment());
            if(totalImages>1) {
                shadingOneBinding.setImageCounterVisibility(true);
                shadingOneBinding.setImageCounterText("+" + (totalImages - 1));
            }
        }

        BeanShade1 beanShade1 = ShadeOne.calculateDimentions(frameModel, width, height);

        final ImageView singleImage = (ImageView) viewSingleBinding.getRoot().findViewById(R.id.view_single_image);


        BindingShadeOne.setLayoutWidth(singleImage, beanShade1.getWidth1());
        BindingShadeOne.setLayoutHeight(singleImage, beanShade1.getHeight1());

        //the calculations were their if needed

        if(!result && bitmap == null)
            bitmap = BitmapFactory.decodeResource(context.getResources(), frameModel.getErrorDrawable());

        if(!hasImageProperties) BindingShadeOne.setBitmap(singleImage, bitmap);
        shadingOneBinding.setImageScaleType(frameModel.getScaleType());

        addImageView(viewSingleBinding.getRoot(), beanShade1.getWidth1(), beanShade1.getHeight1(), false);


        beanBitFrame1.setHeight(/*beanShade1.getHeight1()*/hasImageProperties ? beanBitFrame.getHeight() : bitmap.getHeight());
        beanBitFrame1.setWidth(/*beanShade1.getWidth1()*/hasImageProperties ? beanBitFrame.getWidth() : bitmap.getWidth());
        beanBitFrame1.setImageLink(imageLink1);
        beanBitFrame1.setImageComment(shadingOneBinding.getComment());

        if(!hasImageProperties) Palette.from(bitmap).generate(new PaletteListener(0, this));
        else{
            int resultColor = 0;
            int vibrantColor = beanBitFrame.getVibrantColor();
            int mutedColor = beanBitFrame.getMutedColor();

            beanBitFrame1.setHasGreaterVibrantPopulation(beanBitFrame.isHasGreaterVibrantPopulation());
            switch(frameModel.getColorCombination()){
                case VIBRANT_TO_MUTED:
                    if(beanBitFrame1.isHasGreaterVibrantPopulation())
                        resultColor = vibrantColor;
                    else resultColor = mutedColor;
                    break;
                case MUTED_TO_VIBRANT:
                    if(beanBitFrame1.isHasGreaterVibrantPopulation())
                        resultColor = mutedColor;
                    else resultColor = vibrantColor;
                    break;
            }

            beanBitFrame1.setMutedColor(mutedColor);
            beanBitFrame1.setVibrantColor(vibrantColor);

            //extra properties
            beanBitFrame1.setPrimaryCount(beanBitFrame.getPrimaryCount());
            beanBitFrame1.setSecondaryCount(beanBitFrame.getSecondaryCount());

            shadingOneBinding.setImageBackgroundColor(resultColor);
            shadingOneBinding.setCommentTextBackgroundColor(Utils.getColorWithTransparency(resultColor, frameModel.getCommentTransparencyPercent()));
            setColorsToAddMoreView(resultColor, Utils.getMixedArgbColor(resultColor),
                    Utils.getInverseColor(resultColor));
            frameResult(beanBitFrame1);

            //need to notify ImageShading too, to load image via picasso
            Utils.logVerbose("IMAGE_LOADING : "+" going to load one image");
            final Picasso picasso = getCurrentFramePicasso();
            if(frameModel.isShouldStoreImages()){
                picasso.load(imageLink1).fit().centerInside().noPlaceholder().
                into(singleImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        //do nothing
                        Utils.logMessage("IMAGE_LOADING success");
                    }

                    @Override
                    public void onError() {
                        Utils.logVerbose("IMAGE_LOADING error");
                        picasso.load(imageLink1+"?"+System.currentTimeMillis()).fit().centerInside().noPlaceholder().into(singleImage);
                    }
                });
                /*Picasso.Builder builder = new Picasso.Builder(context.getApplicationContext());
                builder.listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        Utils.logVerbose(uri.getPath()+" link:"+imageLink1);
                        exception.printStackTrace();
                    }
                });
                Picasso picasso1 = builder.build();
                picasso1.setLoggingEnabled(true);
                picasso1.load(imageLink1).into(singleImage);*/

                /*java.io.IOException: Failed to decode stream.
                at com.squareup.picasso.BitmapHunter.decodeStream(BitmapHunter.java:145)
                at com.squareup.picasso.BitmapHunter.hunt(BitmapHunter.java:217)
                at com.squareup.picasso.BitmapHunter.run(BitmapHunter.java:159)
                at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:422)
                at java.util.concurrent.FutureTask.run(FutureTask.java:237)
                at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1112)
                at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:587)
                at java.lang.Thread.run(Thread.java:841)
                at com.squareup.picasso.Utils$PicassoThread.run(Utils.java:411)*/
            }else {
                picasso.load(imageLink1).memoryPolicy(MemoryPolicy.NO_STORE)
                        .networkPolicy(NetworkPolicy.NO_STORE).fit().centerInside().noPlaceholder().
                into(singleImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        //do nothing
                        Utils.logMessage("IMAGE_LOADING success");
                    }

                    @Override
                    public void onError() {
                        Utils.logVerbose("IMAGE_LOADING error");
                        picasso.load(imageLink1+"?"+System.currentTimeMillis()).memoryPolicy(MemoryPolicy.NO_STORE)
                                .networkPolicy(NetworkPolicy.NO_STORE).fit().centerInside().noPlaceholder().into(singleImage);
                    }
                });
            }
        }
        //if(!result) bitmap.recycle();
    }

    @Override
    public void onImageShadeClick(View view) {
        imageClicked(result? ImageType.VIEW_SINGLE : null, 1, imageLink1);
    }

    @Override
    public void onPaletteGenerated(Palette palette, int viewId) throws FrameException{
        int defaultColor = Color.parseColor("#ffffffff");
        int resultColor = 0;
        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
        Palette.Swatch mutedSwatch = palette.getMutedSwatch();
        int vibrantPopulation = vibrantSwatch == null ? 0 : vibrantSwatch.getPopulation();
        int mutedPopulation = mutedSwatch == null ? 0 : mutedSwatch.getPopulation();

        int vibrantColor = palette.getVibrantColor(defaultColor);
        int mutedColor = palette.getMutedColor(defaultColor);

        beanBitFrame1.setHasGreaterVibrantPopulation(vibrantPopulation > mutedPopulation);

        switch(frameModel.getColorCombination()){
            case VIBRANT_TO_MUTED:
                if(beanBitFrame1.isHasGreaterVibrantPopulation())
                    resultColor = vibrantColor;
                else resultColor = mutedColor;
                break;
            case MUTED_TO_VIBRANT:
                if(beanBitFrame1.isHasGreaterVibrantPopulation())
                    resultColor = mutedColor;
                else resultColor = vibrantColor;
                break;
            default:
                throw new FrameException("color combination not defined");
        }

        beanBitFrame1.setMutedColor(mutedColor);
        beanBitFrame1.setVibrantColor(vibrantColor);

        Utils.logMessage("vibrant pop = "+vibrantPopulation+"  muted pop"+mutedPopulation);

        shadingOneBinding.setImageBackgroundColor(resultColor);
        shadingOneBinding.setCommentTextBackgroundColor(Utils.getColorWithTransparency(resultColor, frameModel.getCommentTransparencyPercent()));
        setColorsToAddMoreView(resultColor, Utils.getMixedArgbColor(resultColor),
                Utils.getInverseColor(resultColor));
        frameResult(beanBitFrame1);
    }
}
