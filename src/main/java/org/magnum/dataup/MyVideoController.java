package org.magnum.dataup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpTester.Response;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.lang.RuntimeException;
import java.lang.Exception;

import retrofit.mime.TypedFile;


@Controller
public class MyVideoController {
	 private static final AtomicLong currentId = new AtomicLong(0L);

	  private Map<Long, Video> videos = new HashMap<Long, Video>();

	  @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	  public @ResponseBody Collection<Video> getVideoList() {
	    return videos.values();
	  }

	  @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	  public void getData(@PathVariable("id") long id, HttpServletResponse response)
	      throws Exception {
	    VideoFileManager videoData = VideoFileManager.get();

	    try {
	      videoData.copyVideoData(videos.get(id), response.getOutputStream());
	    } catch (Exception e) {
	    	throw new ResponseStatusException(
	    			  HttpStatus.NOT_FOUND, "entity not found"
	    			);
	    }
	  }

	  @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	  public @ResponseBody Video addVideoMetadata(@RequestBody Video v, HttpServletRequest request)
	      throws IOException {
	    v.setId(currentId.incrementAndGet());
	    v.setDataUrl(getUrlBaseForLocalServer(request) + "/" + VideoSvcApi.VIDEO_SVC_PATH + v.getId() + "/data");
	    videos.put(v.getId(), v);
	    return v;
	  }

	  @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
	  public @ResponseBody VideoStatus addVideoData(@PathVariable("id") long id,
	      @RequestParam MultipartFile data) throws Exception {
	    VideoFileManager videoData = VideoFileManager.get();
	    try {
	      videoData.saveVideoData(videos.get(id), data.getInputStream());
	    } catch (Exception e) {
	      throw new ResponseStatusException(
    			  HttpStatus.NOT_FOUND, "entity not found"
    			);
	    }
	    return new VideoStatus(VideoState.READY);
	  }

	  private String getUrlBaseForLocalServer(HttpServletRequest request) {
	    String baseURL = "http://" + request.getServerName()
	        + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
	    return baseURL;
	  }
}
