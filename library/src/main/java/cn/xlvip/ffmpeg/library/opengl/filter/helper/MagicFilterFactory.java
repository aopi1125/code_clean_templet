package cn.xlvip.ffmpeg.library.opengl.filter.helper;


import cn.xlvip.ffmpeg.library.opengl.filter.base.MagicImageAdjustFilter;
import cn.xlvip.ffmpeg.library.opengl.filter.base.gpuimage.GPUImageBrightnessFilter;
import cn.xlvip.ffmpeg.library.opengl.filter.base.gpuimage.GPUImageContrastFilter;
import cn.xlvip.ffmpeg.library.opengl.filter.base.gpuimage.GPUImageExposureFilter;
import cn.xlvip.ffmpeg.library.opengl.filter.base.gpuimage.GPUImageFilter;
import cn.xlvip.ffmpeg.library.opengl.filter.base.gpuimage.GPUImageHueFilter;
import cn.xlvip.ffmpeg.library.opengl.filter.base.gpuimage.GPUImageSaturationFilter;
import cn.xlvip.ffmpeg.library.opengl.filter.base.gpuimage.GPUImageSharpenFilter;

public class MagicFilterFactory{
	
	private static MagicFilterType filterType = MagicFilterType.NONE;
	
	public static GPUImageFilter initFilters(MagicFilterType type){
		filterType = type;
		switch (type) {
		//image adjust
		case BRIGHTNESS:
			return new GPUImageBrightnessFilter();
		case CONTRAST:
			return new GPUImageContrastFilter();
		case EXPOSURE:
			return new GPUImageExposureFilter();
		case HUE:
			return new GPUImageHueFilter();
		case SATURATION:
			return new GPUImageSaturationFilter();
		case SHARPEN:
			return new GPUImageSharpenFilter();
		case IMAGE_ADJUST:
			return new MagicImageAdjustFilter();
		default:
			return null;
		}
	}
	
	public MagicFilterType getCurrentFilterType(){
		return filterType;
	}
}
